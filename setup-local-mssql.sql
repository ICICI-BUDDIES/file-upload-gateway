-- Setup script for local MSSQL Server
-- Run this in SQL Server Management Studio

-- Create the database
CREATE DATABASE templatedb;
GO

-- Switch to the database
USE templatedb;
GO

-- Create a user for the application
CREATE LOGIN template_user WITH PASSWORD = 'LocalPassword123!';
CREATE USER template_user FOR LOGIN template_user;
ALTER ROLE db_owner ADD MEMBER template_user;
GO

-- Verify setup
SELECT name FROM sys.databases WHERE name = 'templatedb';
SELECT name FROM sys.database_principals WHERE name = 'template_user';
GO

PRINT 'Database setup complete!';
PRINT 'Connection string: Server=localhost;Database=templatedb;User Id=template_user;Password=LocalPassword123!;';