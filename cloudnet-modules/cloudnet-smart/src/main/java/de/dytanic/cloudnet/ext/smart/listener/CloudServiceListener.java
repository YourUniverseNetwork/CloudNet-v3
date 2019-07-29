package de.dytanic.cloudnet.ext.smart.listener;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import de.dytanic.cloudnet.event.service.CloudServiceCreateEvent;
import de.dytanic.cloudnet.event.service.CloudServicePostDeleteEvent;
import de.dytanic.cloudnet.event.service.CloudServicePostPrepareEvent;
import de.dytanic.cloudnet.ext.smart.CloudNetServiceSmartProfile;
import de.dytanic.cloudnet.ext.smart.CloudNetSmartModule;
import de.dytanic.cloudnet.ext.smart.util.SmartServiceTaskConfig;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class CloudServiceListener {

    @EventListener
    public void handleCreateCloudService(CloudServiceCreateEvent event) {
        ServiceTask serviceTask = CloudNet.getInstance().getServiceTask(event.getServiceConfiguration().getServiceId().getTaskName());
        if (serviceTask != null && CloudNetSmartModule.getInstance().hasSmartServiceTaskConfig(serviceTask)) {
            SmartServiceTaskConfig smartTask = CloudNetSmartModule.getInstance().getSmartServiceTaskConfig(serviceTask);

            CloudNetSmartModule.getInstance().updateAsSmartService(event.getServiceConfiguration(), serviceTask, smartTask);
        }
    }

    @EventListener
    public void handle(CloudServicePostPrepareEvent event) {
        ServiceTask task = CloudNet.getInstance().getServiceTask(event.getCloudService().getServiceId().getTaskName());
        if (task != null && CloudNetSmartModule.getInstance().hasSmartServiceTaskConfig(task)) {
            SmartServiceTaskConfig smartTask = CloudNetSmartModule.getInstance().getSmartServiceTaskConfig(task);

            UUID uniqueId = event.getCloudService().getServiceId().getUniqueId();

            if (smartTask.isDirectTemplatesAndInclusionsSetup()) {
                CloudNetDriver.getInstance().includeWaitingServiceTemplates(uniqueId);
                CloudNetDriver.getInstance().includeWaitingServiceInclusions(uniqueId);
            }

            CloudNetSmartModule.getInstance().getProvidedSmartServices().put(uniqueId, new CloudNetServiceSmartProfile(
                    uniqueId,
                    new AtomicInteger(smartTask.getAutoStopTimeByUnusedServiceInSeconds())
            ));
        }
    }

    @EventListener
    public void handle(CloudServicePostDeleteEvent event) {
        CloudNetSmartModule.getInstance().getProvidedSmartServices().remove(event.getCloudService().getServiceId().getUniqueId());
    }
}