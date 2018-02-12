var collectConnectionPoints = function(resourceModel, diff) {
    return collectPorts(resourceModel, diff)
};

function collectPorts(resourceModel, diff){
    pathToResource = {}
    collectResources('', resourceModel, pathToResource, true);
    transformedPorts = []
    Object.keys(pathToResource).forEach(function (path) {
        var port = pathToResource[path];
        transformedPort = {}
        transformedPort.name = port.attributes.name;
        transformedPort.providerId = port.attributes.id;
        transformedPort.cpId = path;
        var managedPort = false;
        if(port.hasOwnProperty('externalConnectionPoint')){
            transformedPort.ecpdId = port.externalConnectionPoint;
            managedPort = true;
        }
        if(port.hasOwnProperty('connectionPoint')){
            transformedPort.cpdId = port.connectionPoint;
            managedPort = true;
        }
        transformedPort.tenantId = port.attributes.tenant_id;
        transformedPort.ipAddress = port.attributes.fixed_ips[0].ip_address;
        transformedPort.macAddress = port.attributes.mac_address;
        transformedPort.serverProviderId = port.attributes.device_id;
        transformedPort.networkProviderId = port.attributes.network_id;
        transformedPort.changeType = 'untouched';
        var added = contains(diff.add, path);
        var removed = contains(diff.remove, path);
        if(added && removed){
            transformedPort.changeType = "MODIFIED";
        }
        else{
            if(removed){
                transformedPort.changeType = "REMOVED";
            }
            if(added){
                transformedPort.changeType = "ADDED";
            }
        }
        if('untouched' != transformedPort.changeType && managedPort){
            transformedPorts.push(transformedPort)
        }
    })
    return transformedPorts;
};

function contains(resourceChanges, path){
    var keys = Object.keys(resourceChanges);
    return keys.indexOf(path) !== -1;
}

function collectResources(path, root, pathToResouceMap, onResources){
    root && Object.keys(root).forEach(function(item) {
        if(item == 'resource_type' && root[item] == 'OS::Neutron::Port'){
            pathToResouceMap[path] = root
        }
        else if(typeof root[item] === "object"){
            var newItem = onResources ? "" : item;
            var newPath = path;
            if('' != newItem && path != ''){
                newPath += ".";
            }
            newPath += newItem;
            collectResources(newPath, root[item], pathToResouceMap, !onResources)
        }
    });
};

