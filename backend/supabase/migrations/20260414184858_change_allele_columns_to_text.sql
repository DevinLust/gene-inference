-- Sheep distribution
alter table sheep_distribution
    alter column allele_code type text;

-- Sheep genotype
alter table sheep_genotype
    alter column phenotype type text,
    alter column hidden type text;

-- Birth record phenotype
alter table birth_record_phenotype
    alter column parent1_phenotype type text,
    alter column parent2_phenotype type text,
    alter column child_phenotype type text;
