package com.attendance.service;

import com.attendance.entity.SystemConfig;
import com.attendance.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    public LocalTime getWorkStartTime() {
        SystemConfig config = systemConfigRepository.findByConfigKey("work_start_time")
                .orElseGet(() -> {
                    SystemConfig defaultConfig = new SystemConfig();
                    defaultConfig.setConfigKey("work_start_time");
                    defaultConfig.setConfigValue("09:00:00");
                    defaultConfig.setDescription("上班时间");
                    return systemConfigRepository.save(defaultConfig);
                });
        return LocalTime.parse(config.getConfigValue());
    }

    public LocalTime getWorkEndTime() {
        SystemConfig config = systemConfigRepository.findByConfigKey("work_end_time")
                .orElseGet(() -> {
                    SystemConfig defaultConfig = new SystemConfig();
                    defaultConfig.setConfigKey("work_end_time");
                    defaultConfig.setConfigValue("18:00:00");
                    defaultConfig.setDescription("下班时间");
                    return systemConfigRepository.save(defaultConfig);
                });
        return LocalTime.parse(config.getConfigValue());
    }

    @Transactional
    public void setWorkStartTime(LocalTime time) {
        SystemConfig config = systemConfigRepository.findByConfigKey("work_start_time")
                .orElse(new SystemConfig());
        config.setConfigKey("work_start_time");
        config.setConfigValue(time.toString());
        config.setDescription("上班时间");
        systemConfigRepository.save(config);
    }

    @Transactional
    public void setWorkEndTime(LocalTime time) {
        SystemConfig config = systemConfigRepository.findByConfigKey("work_end_time")
                .orElse(new SystemConfig());
        config.setConfigKey("work_end_time");
        config.setConfigValue(time.toString());
        config.setDescription("下班时间");
        systemConfigRepository.save(config);
    }
}