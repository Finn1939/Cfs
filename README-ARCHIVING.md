Order archiving
===============

This change introduces a lightweight order archiving feature.

Defaults
- Archiving enabled: true
- Auto-archive after: 180 days

Configuration (application.properties)
- cfs.archive.enabled=true
- cfs.archive.days=180

Notes
- The implementation ships with a service and controller (OrderArchiveService, OrderArchiveController).
- The migration adds an 'archived' boolean column to the orders table. Adjust the migration if your project uses Liquibase/Flyway templates.
