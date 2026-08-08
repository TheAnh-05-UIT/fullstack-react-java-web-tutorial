output "vpc_id" { value = aws_vpc.this.id }
output "vpc_cidr" { value = aws_vpc.this.cidr_block }
output "public_subnet_ids" { value = aws_subnet.public[*].id }
output "app_subnet_ids" { value = aws_subnet.app[*].id }
output "db_subnet_ids" { value = aws_subnet.db[*].id }
output "db_subnet_group_name" { value = aws_db_subnet_group.this.name }
output "nat_gateway_id" { value = try(aws_nat_gateway.this[0].id, null) }
