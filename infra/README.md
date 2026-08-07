# Infrastructure as Code (IaC) for MediLine

This directory contains Azure Bicep templates for deploying the MediLine application.

## Files

- `main.bicep` - Main template that orchestrates deployment
- `roleAssignment.bicep` - Module for managing Azure role assignments
- `parameters.json` - Parameters file for deployments

## Deployment

### Prerequisites

1. Azure CLI installed and authenticated:
   ```bash
   az login
   ```

2. Resource group created:
   ```bash
   az group create --name mediline-rg --location eastus
   ```

### Deploy

1. Update `parameters.json` with your specific values:
   - `principalIds`: Array of Azure object IDs (users/service principals)
   - `roleAssignmentGuids`: Array of unique GUIDs for role assignments
   - `location`: Azure region for resources
   - `environment`: Deployment environment (dev/staging/prod)

2. Deploy using Azure CLI:
   ```bash
   az deployment group create \
     --resource-group mediline-rg \
     --template-file infra/main.bicep \
     --parameters @infra/parameters.json
   ```

### Get Principal ID

To find a user's or service principal's object ID:

**For a user:**
```bash
az ad user show --id user@example.com --query id -o tsv
```

**For a service principal:**
```bash
az ad sp show --id /subscriptions/{subscription}/resourcegroups/{rg}/providers/microsoft.managedidentity/userassignedidentities/{identity-name} --query id -o tsv
```

**For a group:**
```bash
az ad group show --group "group-name" --query id -o tsv
```

### Common Role Definition IDs

- `b24988ac-6180-42a0-ab88-20f7382dd24c` - Contributor
- `acdd72a7-3385-48ef-bd42-f606fba81ae7` - Reader
- `8e3af657-a8ff-443c-a75c-2fe8c4bcb635` - Owner

## Troubleshooting

If deployment fails, check:
1. Azure CLI is logged in: `az account show`
2. Resource group exists: `az group show --name mediline-rg`
3. Principal IDs are valid
4. Role assignment GUIDs are unique
