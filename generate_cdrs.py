import random
from datetime import datetime, timedelta

# Configuration
contract_id = 1
msisdn = "201055556666"
total_records = 200
output_file = "/opt/billing/cdr/bulk_test_04.csv" 

# Start time: June 1st, 2026
current_time = datetime(2026, 6, 1, 8, 0, 0)

with open(output_file, 'w') as f:
    # 1. Header row
    header = "contract_id,dial_a,dial_b,service_type,duration,start_time,external_fee_piasters,is_rated\n"
    f.write(header)
    
    for i in range(total_records):
        service_type = random.choice([1, 2, 3])
        
        if service_type == 1: # Voice (Minutes)
            quantity = round(random.uniform(1.0, 10.0), 2)
            dial_b = "2011" + str(random.randint(10000000, 99999999)) 
            
        elif service_type == 2: # SMS (Count)
            quantity = 1.0
            dial_b = "2012" + str(random.randint(10000000, 99999999))
            
        else: # Data (Bytes)
            # Generate between 1 MB (1,048,576 bytes) and 50 MB (52,428,800 bytes)
            quantity = random.randint(1048576, 52428800)
            dial_b = "internet.vodafone.net"
            
        current_time += timedelta(minutes=random.randint(1, 15))
        
        # 2. Write the line
        line = f"{contract_id},{msisdn},{dial_b},{service_type},{quantity},{current_time.strftime('%Y-%m-%d %H:%M:%S')},0,false\n"
        f.write(line)

print(f"✅ Generated {total_records} CDRs in {output_file}")
