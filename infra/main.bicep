@description('Location for all resources')
param location string = resourceGroup().location

@description('Application name')
param appName string = 'mediline'

@description('Environment name (dev, staging, prod)')
param environment string = 'dev'

@description('The principal ID (objectId) of the user, group, or service principal to assign roles to.')
param principalIds array

@description('A unique GUID for the role assignment.')
param roleAssignmentGuids array

// Reference the role assignment module
module roleAssignments './roleAssignment.bicep' = [for (principalId, index) in principalIds: {
  name: 'roleAssignment-${index}'
  params: {
    principalId: principalId
    roleDefinitionId: 'b24988ac-6180-42a0-ab88-20f7382dd24c' // Contributor role
    scope: resourceGroup().id
    roleAssignmentGuid: roleAssignmentGuids[index]
  }
}]

output deploymentName string = '${appName}-${environment}'
output roleAssignmentIds array = [for (index, _) in range(0, length(principalIds)): roleAssignments[index].outputs.roleAssignmentId]
