package com.github.bingoohuang.blackcat.server.eventlistener;

import com.github.bingoohuang.blackcat.sdk.utils.StrBuilder;
import com.github.bingoohuang.blackcat.server.base.BlackcatReqListener;
import com.github.bingoohuang.blackcat.server.base.MsgService;
import com.github.bingoohuang.blackcat.server.dao.EventDao;
import com.github.bingoohuang.blackcat.server.domain.BlackcatConfigBean;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventLast;
import com.github.bingoohuang.blackcat.server.domain.BlackcatEventType;
import com.github.bingoohuang.blackcat.server.domain.BlackcatFileStoresReq;
import com.github.bingoohuang.blackcat.server.job.AbstractMsgJob;
import com.google.common.eventbus.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.github.bingoohuang.blackcat.sdk.utils.Blackcats.prettyBytes;

@Component
public class BlackcatFileStoresReqListener implements BlackcatReqListener {
    @Autowired MsgService msgService;
    @Autowired EventDao eventDao;
    @Autowired BlackcatConfigBean bean;

    @Subscribe
    public void deal(BlackcatFileStoresReq req) {
        String hostname = req.getHostname();
        bean.getTimes().add(hostname + AbstractMsgJob.FILE_STORES_TIMES);
        bean.getBeats().put(hostname, System.currentTimeMillis());

        tryAlert(req);

        eventDao.updateEventLast(new BlackcatEventLast(
                hostname, BlackcatEventType.FileStore.toString(), req.getTimestamp()));
        eventDao.addFileStoresEvent(req);
    }

    private void tryAlert(BlackcatFileStoresReq req) {
        long configLowSize = bean.getConfigThreshold().getAvailDiskLowSize();
        int configLowRatio = bean.getConfigThreshold().getAvailDiskLowRatio();

        StrBuilder alert = StrBuilder.str('\n').p(req.getHostname());
        boolean hasAlert = false;

        for (int i = 0, ii = req.getNames().size(); i < ii; ++i) {
            String fileStoreName = req.getNames().get(i);
            if ("tmpfs".equals(fileStoreName)) continue;

            Long avail = req.getUsables().get(i);
            boolean isLowSize = avail < configLowSize;
            long ratio = avail * 100 / req.getTotals().get(i);
            boolean isLowRatio = ratio < configLowRatio;
            if (!isLowSize && !isLowRatio) continue;

            hasAlert = true;
            alert.p('\n').p(fileStoreName).p("可用").p(prettyBytes(avail));

            if (isLowSize) alert.p(", 低于阀值").p(prettyBytes(configLowSize));
            if (isLowRatio) alert.p(", 低于比例").p(ratio).p('%');
        }

        if (!hasAlert) return;

        bean.getTimes().add(req.getHostname() + AbstractMsgJob.FILE_STORES_ALERTS);
        msgService.sendMsg("磁盘告警", alert.toString());
    }

}
