@description('The principal ID (objectId) of the user, group, or service principal to assign the role to.')
param principalId string

@description('The role definition ID to assign. Examples: Contributor, Reader, Owner, etc.')
param roleDefinitionId string

@description('The scope at which the role assignment applies. For example, a subscription, resource group, or resource ID.')
param scope string

@description('A unique GUID for the role assignment.')
param roleAssignmentGuid string

resource roleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: roleAssignmentGuid
  scope: resourceGroup()
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', roleDefinitionId)
    principalId: principalId
    principalType: 'User' // Can be User, Group, ServicePrincipal, or ForeignGroup
  }
}

output roleAssignmentId string = roleAssignment.id
