-- Add an archived flag to orders for simple archiving
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS archived BOOLEAN DEFAULT FALSE;