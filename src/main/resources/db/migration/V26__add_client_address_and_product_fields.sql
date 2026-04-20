-- Add address fields to clients table
ALTER TABLE clients ADD COLUMN IF NOT EXISTS cpf_cnpj VARCHAR(20);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS address_number VARCHAR(20);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS complement VARCHAR(100);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS neighborhood VARCHAR(100);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS state VARCHAR(2);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS zip_code VARCHAR(10);

-- Add extra fields to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS dimensions VARCHAR(100);
ALTER TABLE products ADD COLUMN IF NOT EXISTS weight DOUBLE PRECISION;
ALTER TABLE products ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Change base_price from double to numeric for precision
ALTER TABLE products ALTER COLUMN base_price TYPE NUMERIC(15,2) USING base_price::NUMERIC(15,2);

-- Change total_value from double to numeric for precision
ALTER TABLE orders ALTER COLUMN total_value TYPE NUMERIC(15,2) USING total_value::NUMERIC(15,2);
