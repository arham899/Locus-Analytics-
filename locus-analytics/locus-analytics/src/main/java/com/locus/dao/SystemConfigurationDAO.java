package com.locus.dao;
import com.locus.model.SystemConfiguration;
import java.util.List;

public interface SystemConfigurationDAO {

    SystemConfiguration getConfig(String configId);

    boolean update(SystemConfiguration config);

    List<SystemConfiguration> getAllConfigs();
}
