ALTER TABLE session ADD COLUMN IF NOT EXISTS track_temperature INTEGER;
ALTER TABLE session ADD COLUMN IF NOT EXISTS air_temperature INTEGER;
ALTER TABLE session ADD COLUMN IF NOT EXISTS safety_car_status INTEGER;
ALTER TABLE session ADD COLUMN IF NOT EXISTS num_safety_car_periods INTEGER;
ALTER TABLE session ADD COLUMN IF NOT EXISTS num_virtual_safety_car_periods INTEGER;
ALTER TABLE session ADD COLUMN IF NOT EXISTS num_red_flag_periods INTEGER;
