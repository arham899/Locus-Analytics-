package com.locus.ui.controller;

import com.locus.ui.ServiceRegistry;

/**
 * Marker interface for controllers that need access to shared services.
 */
public interface ServiceAwareController {
    void setServiceRegistry(ServiceRegistry serviceRegistry);
}
