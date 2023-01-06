package com.bienao.robot.service.async;

import com.bienao.robot.entity.QlEntity;

import java.util.List;

public interface AsyncService {
    void updateWxpusherUid(List<QlEntity> qls, String ptPin, String code);
}
