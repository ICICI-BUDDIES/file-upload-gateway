-- Simple MSSQL Database Setup
-- Run this on your MSSQL Server before deploying

CREATE DATABASE templatedb;
GO

USE templatedb;
GO

-- Create user (optional - can use sa account)
CREATE LOGIN template_user WITH PASSWORD = 'YourSecurePassword123!';
CREATE USER template_user FOR LOGIN template_user;
ALTER ROLE db_owner ADD MEMBER template_user;
GO