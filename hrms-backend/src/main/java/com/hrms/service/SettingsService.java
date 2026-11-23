package com.hrms.service;

import com.hrms.entity.AppSetting;
import com.hrms.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService {

    private final AppSettingRepository settingRepository;

    public SettingsService(AppSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public Map<String, String> getAllSettings() {
        List<AppSetting> settings = settingRepository.findAll();
        Map<String, String> map = new HashMap<>();
        for (AppSetting s : settings) {
            map.put(s.getKey(), s.getValue());
        }
        return map;
    }

    @Transactional
    public void upsertSettings(Map<String, Object> newSettings) {
        for (Map.Entry<String, Object> entry : newSettings.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";

            AppSetting setting = settingRepository.findByKey(key)
                    .orElseGet(() -> new AppSetting(key, val));
            setting.setValue(val);
            settingRepository.save(setting);
        }
    }

    public Map<String, Object> getIntegrationConfig() {
        Map<String, String> all = getAllSettings();
        Map<String, Object> s3 = new HashMap<>();
        s3.put("bucket", all.getOrDefault("s3_bucket", ""));
        s3.put("region", all.getOrDefault("s3_region", ""));
        s3.put("accessKeyId", all.getOrDefault("s3_access_key", ""));
        s3.put("secretAccessKey", all.containsKey("s3_secret_key") ? "••••••••" : "");
        s3.put("endpoint", all.getOrDefault("s3_endpoint", ""));
        s3.put("prefix", all.getOrDefault("s3_prefix", "documents"));

        Map<String, Object> res = new HashMap<>();
        res.put("documentStorageEnabled", Boolean.parseBoolean(all.getOrDefault("doc_storage_enabled", "false")));
        res.put("s3", s3);
        return res;
    }
}
