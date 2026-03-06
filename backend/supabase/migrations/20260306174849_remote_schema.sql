

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;


COMMENT ON SCHEMA "public" IS 'standard public schema';



CREATE EXTENSION IF NOT EXISTS "pg_graphql" WITH SCHEMA "graphql";






CREATE EXTENSION IF NOT EXISTS "pg_stat_statements" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "pgcrypto" WITH SCHEMA "extensions";






CREATE EXTENSION IF NOT EXISTS "supabase_vault" WITH SCHEMA "vault";






CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA "extensions";






CREATE TYPE "public"."category" AS ENUM (
    'SWIM',
    'FLY',
    'RUN',
    'POWER',
    'STAMINA'
);


ALTER TYPE "public"."category" OWNER TO "postgres";


CREATE TYPE "public"."distribution_type" AS ENUM (
    'PRIOR',
    'INFERRED'
);


ALTER TYPE "public"."distribution_type" OWNER TO "postgres";


CREATE TYPE "public"."grade" AS ENUM (
    'S',
    'A',
    'B',
    'C',
    'D',
    'E'
);


ALTER TYPE "public"."grade" OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."get_birth_phenotypes_for_relationship"("p_relationship_id" integer) RETURNS TABLE("category" "text", "parent1_phenotype" "text", "parent2_phenotype" "text", "child_phenotype" "text", "frequency" bigint)
    LANGUAGE "plpgsql"
    AS $$
BEGIN
    RETURN QUERY
    SELECT
	    brp.category,
	    brp.parent1_phenotype,
	    brp.parent2_phenotype,
	    brp.child_phenotype,
	    COUNT(*) AS frequency
	FROM birth_record br
	JOIN birth_record_phenotype brp
	  ON br.id = brp.birth_record_id
	WHERE br.relationship_id = p_relationship_id
	GROUP BY
	    brp.category,
	    brp.parent1_phenotype,
	    brp.parent2_phenotype,
	    brp.child_phenotype
	ORDER BY
	    brp.category,
	    brp.parent1_phenotype,
	    brp.parent2_phenotype,
	    brp.child_phenotype;
END;
$$;


ALTER FUNCTION "public"."get_birth_phenotypes_for_relationship"("p_relationship_id" integer) OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."get_children_of_sheep"("parent_id" integer) RETURNS TABLE("child_id" integer, "child_name" "text")
    LANGUAGE "plpgsql"
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        child.id,
        child.name
    FROM relationship r
	JOIN birth_record br ON br.relationship_id = r.id
    JOIN sheep child ON child.id = br.sheep_id
    WHERE r.parent1_id = parent_id OR r.parent2_id = parent_id;
END;
$$;


ALTER FUNCTION "public"."get_children_of_sheep"("parent_id" integer) OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."get_sheep_with_distribution"("cat" "text", "dist_type" "text") RETURNS TABLE("sheep_id" integer, "name" "text", "phenotype" character varying, "hidden_allele" character varying, "grade_s" double precision, "grade_a" double precision, "grade_b" double precision, "grade_c" double precision, "grade_d" double precision, "grade_e" double precision)
    LANGUAGE "plpgsql"
    AS $$
BEGIN
    RETURN QUERY
    SELECT
        s.id AS sheep_id,
        s.name,
        g.phenotype,
        g.hidden,
        dist.grade_s,
        dist.grade_a,
        dist.grade_b,
        dist.grade_c,
        dist.grade_d,
        dist.grade_e
    FROM sheep s
    LEFT JOIN sheep_genotype g
        ON s.id = g.sheep_id AND g.category = cat
    LEFT JOIN (
        SELECT
            d.sheep_id,
            MAX(probability) FILTER (WHERE grade = 'S') AS grade_s,
            MAX(probability) FILTER (WHERE grade = 'A') AS grade_a,
            MAX(probability) FILTER (WHERE grade = 'B') AS grade_b,
            MAX(probability) FILTER (WHERE grade = 'C') AS grade_c,
            MAX(probability) FILTER (WHERE grade = 'D') AS grade_d,
            MAX(probability) FILTER (WHERE grade = 'E') AS grade_e
        FROM sheep_distribution d
        WHERE d.category = cat AND d.distribution_type = dist_type
        GROUP BY d.sheep_id
    ) dist ON s.id = dist.sheep_id
    ORDER BY s.id;
END;
$$;


ALTER FUNCTION "public"."get_sheep_with_distribution"("cat" "text", "dist_type" "text") OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."handle_new_user"() RETURNS "trigger"
    LANGUAGE "plpgsql" SECURITY DEFINER
    SET "search_path" TO 'public'
    AS $$
begin
  insert into public.profiles (id)
  values (new.id)
  on conflict (id) do nothing;

  return new;
end;
$$;


ALTER FUNCTION "public"."handle_new_user"() OWNER TO "postgres";


CREATE OR REPLACE FUNCTION "public"."pivot_relationship_distribution"() RETURNS TABLE("relationship_id" integer, "prob_s_s" double precision, "prob_s_a" double precision, "prob_s_b" double precision, "prob_s_c" double precision, "prob_s_d" double precision, "prob_s_e" double precision, "prob_a_s" double precision, "prob_a_a" double precision, "prob_a_b" double precision, "prob_a_c" double precision, "prob_a_d" double precision, "prob_a_e" double precision, "prob_b_s" double precision, "prob_b_a" double precision, "prob_b_b" double precision, "prob_b_c" double precision, "prob_b_d" double precision, "prob_b_e" double precision, "prob_c_s" double precision, "prob_c_a" double precision, "prob_c_b" double precision, "prob_c_c" double precision, "prob_c_d" double precision, "prob_c_e" double precision, "prob_d_s" double precision, "prob_d_a" double precision, "prob_d_b" double precision, "prob_d_c" double precision, "prob_d_d" double precision, "prob_d_e" double precision, "prob_e_s" double precision, "prob_e_a" double precision, "prob_e_b" double precision, "prob_e_c" double precision, "prob_e_d" double precision, "prob_e_e" double precision)
    LANGUAGE "plpgsql"
    AS $$
DECLARE
    sql TEXT := 'SELECT relationship_id';
    g1 TEXT;
    g2 TEXT;
BEGIN
    FOR g1 IN SELECT unnest(enum_range(NULL::grade)) LOOP
        FOR g2 IN SELECT unnest(enum_range(NULL::grade)) LOOP
            sql := sql || FORMAT(
                ', COALESCE(MAX(probability) FILTER (WHERE grade1 = ''%s'' AND grade2 = ''%s''), 0) AS prob_%s_%s',
                g1, g2, g1, g2
            );
        END LOOP;
    END LOOP;

    sql := sql || ' FROM relationship_hidden_pairs_distribution GROUP BY relationship_id';

    RETURN QUERY EXECUTE sql;
END
$$;


ALTER FUNCTION "public"."pivot_relationship_distribution"() OWNER TO "postgres";

SET default_tablespace = '';

SET default_table_access_method = "heap";


CREATE TABLE IF NOT EXISTS "public"."birth_record" (
    "id" integer NOT NULL,
    "relationship_id" integer NOT NULL,
    "sheep_id" integer
);


ALTER TABLE "public"."birth_record" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."birth_record_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "public"."birth_record_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."birth_record_id_seq" OWNED BY "public"."birth_record"."id";



CREATE TABLE IF NOT EXISTS "public"."birth_record_phenotype" (
    "birth_record_id" integer NOT NULL,
    "category" "text" NOT NULL,
    "parent1_phenotype" "text" NOT NULL,
    "parent2_phenotype" "text" NOT NULL,
    "child_phenotype" "text" NOT NULL
);


ALTER TABLE "public"."birth_record_phenotype" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."profiles" (
    "id" "uuid" NOT NULL,
    "first_name" "text",
    "last_name" "text"
);


ALTER TABLE "public"."profiles" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."relationship" (
    "id" integer NOT NULL,
    "parent1_id" integer NOT NULL,
    "parent2_id" integer NOT NULL,
    CONSTRAINT "relationship_no_self_ck" CHECK (("parent1_id" <> "parent2_id")),
    CONSTRAINT "relationship_parent_order_ck" CHECK (("parent1_id" < "parent2_id"))
);

ALTER TABLE ONLY "public"."relationship" FORCE ROW LEVEL SECURITY;


ALTER TABLE "public"."relationship" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."relationship_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "public"."relationship_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."relationship_id_seq" OWNED BY "public"."relationship"."id";



CREATE TABLE IF NOT EXISTS "public"."sheep" (
    "id" integer NOT NULL,
    "name" "text",
    "user_id" "uuid" NOT NULL
);


ALTER TABLE "public"."sheep" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."sheep_distribution" (
    "sheep_id" integer NOT NULL,
    "category" "text" NOT NULL,
    "distribution_type" "text" NOT NULL,
    "grade" character varying(1) NOT NULL,
    "probability" double precision
);


ALTER TABLE "public"."sheep_distribution" OWNER TO "postgres";


CREATE TABLE IF NOT EXISTS "public"."sheep_genotype" (
    "sheep_id" integer NOT NULL,
    "category" "text" NOT NULL,
    "phenotype" character varying(1),
    "hidden" character varying(1)
);


ALTER TABLE "public"."sheep_genotype" OWNER TO "postgres";


CREATE SEQUENCE IF NOT EXISTS "public"."sheep_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "public"."sheep_id_seq" OWNER TO "postgres";


ALTER SEQUENCE "public"."sheep_id_seq" OWNED BY "public"."sheep"."id";



ALTER TABLE ONLY "public"."birth_record" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."birth_record_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."relationship" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."relationship_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."sheep" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."sheep_id_seq"'::"regclass");



ALTER TABLE ONLY "public"."birth_record_phenotype"
    ADD CONSTRAINT "birth_record_phenotype_pkey" PRIMARY KEY ("birth_record_id", "category");



ALTER TABLE ONLY "public"."birth_record"
    ADD CONSTRAINT "birth_record_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."birth_record"
    ADD CONSTRAINT "birth_record_sheep_id_key" UNIQUE ("sheep_id");



ALTER TABLE ONLY "public"."profiles"
    ADD CONSTRAINT "profiles_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."relationship"
    ADD CONSTRAINT "relationship_parent_pair_uk" UNIQUE ("parent1_id", "parent2_id");



ALTER TABLE ONLY "public"."relationship"
    ADD CONSTRAINT "relationship_pkey" PRIMARY KEY ("id");



ALTER TABLE ONLY "public"."sheep_distribution"
    ADD CONSTRAINT "sheep_distribution_pkey" PRIMARY KEY ("sheep_id", "category", "distribution_type", "grade");



ALTER TABLE ONLY "public"."sheep_genotype"
    ADD CONSTRAINT "sheep_genotype_pkey" PRIMARY KEY ("sheep_id", "category");



ALTER TABLE ONLY "public"."sheep"
    ADD CONSTRAINT "sheep_pkey" PRIMARY KEY ("id");



CREATE INDEX "idx_distribution_category_type" ON "public"."sheep_distribution" USING "btree" ("category", "distribution_type");



CREATE INDEX "idx_distribution_sheep_cat_type" ON "public"."sheep_distribution" USING "btree" ("sheep_id", "category", "distribution_type");



CREATE INDEX "idx_genotype_category" ON "public"."sheep_genotype" USING "btree" ("category");



CREATE INDEX "idx_genotype_sheep" ON "public"."sheep_genotype" USING "btree" ("sheep_id");



CREATE INDEX "idx_relationship_parent1" ON "public"."relationship" USING "btree" ("parent1_id");



CREATE INDEX "idx_relationship_parent2" ON "public"."relationship" USING "btree" ("parent2_id");



CREATE INDEX "idx_sheep_user_id" ON "public"."sheep" USING "btree" ("user_id");



ALTER TABLE ONLY "public"."birth_record_phenotype"
    ADD CONSTRAINT "birth_record_phenotype_birth_record_id_fkey" FOREIGN KEY ("birth_record_id") REFERENCES "public"."birth_record"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."birth_record"
    ADD CONSTRAINT "birth_record_relationship_id_fkey" FOREIGN KEY ("relationship_id") REFERENCES "public"."relationship"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."birth_record"
    ADD CONSTRAINT "birth_record_sheep_id_fkey" FOREIGN KEY ("sheep_id") REFERENCES "public"."sheep"("id") ON DELETE SET NULL;



ALTER TABLE ONLY "public"."relationship"
    ADD CONSTRAINT "fk_parent1" FOREIGN KEY ("parent1_id") REFERENCES "public"."sheep"("id");



ALTER TABLE ONLY "public"."relationship"
    ADD CONSTRAINT "fk_parent2" FOREIGN KEY ("parent2_id") REFERENCES "public"."sheep"("id");



ALTER TABLE ONLY "public"."profiles"
    ADD CONSTRAINT "profiles_id_fkey" FOREIGN KEY ("id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;



ALTER TABLE ONLY "public"."sheep_distribution"
    ADD CONSTRAINT "sheep_distribution_sheep_id_fkey" FOREIGN KEY ("sheep_id") REFERENCES "public"."sheep"("id");



ALTER TABLE ONLY "public"."sheep_genotype"
    ADD CONSTRAINT "sheep_genotype_sheep_id_fkey" FOREIGN KEY ("sheep_id") REFERENCES "public"."sheep"("id");



ALTER TABLE ONLY "public"."sheep"
    ADD CONSTRAINT "sheep_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."profiles"("id") ON DELETE CASCADE;



ALTER TABLE "public"."birth_record" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."birth_record_phenotype" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."profiles" ENABLE ROW LEVEL SECURITY;


CREATE POLICY "profiles_insert_auth_admin" ON "public"."profiles" FOR INSERT TO "supabase_auth_admin" WITH CHECK (true);



CREATE POLICY "profiles_insert_authenticator" ON "public"."profiles" FOR INSERT TO "authenticator" WITH CHECK (true);



CREATE POLICY "profiles_select_own" ON "public"."profiles" FOR SELECT USING (("id" = "auth"."uid"()));



CREATE POLICY "profiles_update_own" ON "public"."profiles" FOR UPDATE USING (("id" = "auth"."uid"())) WITH CHECK (("id" = "auth"."uid"()));



ALTER TABLE "public"."relationship" ENABLE ROW LEVEL SECURITY;


CREATE POLICY "relationship_insert_own_parents" ON "public"."relationship" FOR INSERT TO "authenticated" WITH CHECK (((EXISTS ( SELECT 1
   FROM "public"."sheep" "s"
  WHERE (("s"."id" = "relationship"."parent1_id") AND ("s"."user_id" = "auth"."uid"())))) AND (EXISTS ( SELECT 1
   FROM "public"."sheep" "s"
  WHERE (("s"."id" = "relationship"."parent2_id") AND ("s"."user_id" = "auth"."uid"()))))));



CREATE POLICY "relationship_select_own" ON "public"."relationship" FOR SELECT TO "authenticated" USING (((EXISTS ( SELECT 1
   FROM "public"."sheep" "s"
  WHERE (("s"."id" = "relationship"."parent1_id") AND ("s"."user_id" = "auth"."uid"())))) AND (EXISTS ( SELECT 1
   FROM "public"."sheep" "s"
  WHERE (("s"."id" = "relationship"."parent2_id") AND ("s"."user_id" = "auth"."uid"()))))));



ALTER TABLE "public"."sheep" ENABLE ROW LEVEL SECURITY;


CREATE POLICY "sheep_delete_own" ON "public"."sheep" FOR DELETE USING (("user_id" = "auth"."uid"()));



ALTER TABLE "public"."sheep_distribution" ENABLE ROW LEVEL SECURITY;


ALTER TABLE "public"."sheep_genotype" ENABLE ROW LEVEL SECURITY;


CREATE POLICY "sheep_insert_own" ON "public"."sheep" FOR INSERT WITH CHECK (("user_id" = "auth"."uid"()));



CREATE POLICY "sheep_select_own" ON "public"."sheep" FOR SELECT USING (("user_id" = "auth"."uid"()));



CREATE POLICY "sheep_update_own" ON "public"."sheep" FOR UPDATE USING (("user_id" = "auth"."uid"())) WITH CHECK (("user_id" = "auth"."uid"()));





ALTER PUBLICATION "supabase_realtime" OWNER TO "postgres";


GRANT USAGE ON SCHEMA "public" TO "postgres";
GRANT USAGE ON SCHEMA "public" TO "anon";
GRANT USAGE ON SCHEMA "public" TO "authenticated";
GRANT USAGE ON SCHEMA "public" TO "service_role";
GRANT USAGE ON SCHEMA "public" TO "supabase_auth_admin";
GRANT USAGE ON SCHEMA "public" TO "authenticator";

























































































































































GRANT ALL ON FUNCTION "public"."get_birth_phenotypes_for_relationship"("p_relationship_id" integer) TO "anon";
GRANT ALL ON FUNCTION "public"."get_birth_phenotypes_for_relationship"("p_relationship_id" integer) TO "authenticated";
GRANT ALL ON FUNCTION "public"."get_birth_phenotypes_for_relationship"("p_relationship_id" integer) TO "service_role";



GRANT ALL ON FUNCTION "public"."get_children_of_sheep"("parent_id" integer) TO "anon";
GRANT ALL ON FUNCTION "public"."get_children_of_sheep"("parent_id" integer) TO "authenticated";
GRANT ALL ON FUNCTION "public"."get_children_of_sheep"("parent_id" integer) TO "service_role";



GRANT ALL ON FUNCTION "public"."get_sheep_with_distribution"("cat" "text", "dist_type" "text") TO "anon";
GRANT ALL ON FUNCTION "public"."get_sheep_with_distribution"("cat" "text", "dist_type" "text") TO "authenticated";
GRANT ALL ON FUNCTION "public"."get_sheep_with_distribution"("cat" "text", "dist_type" "text") TO "service_role";



GRANT ALL ON FUNCTION "public"."handle_new_user"() TO "anon";
GRANT ALL ON FUNCTION "public"."handle_new_user"() TO "authenticated";
GRANT ALL ON FUNCTION "public"."handle_new_user"() TO "service_role";



GRANT ALL ON FUNCTION "public"."pivot_relationship_distribution"() TO "anon";
GRANT ALL ON FUNCTION "public"."pivot_relationship_distribution"() TO "authenticated";
GRANT ALL ON FUNCTION "public"."pivot_relationship_distribution"() TO "service_role";


















GRANT ALL ON TABLE "public"."birth_record" TO "anon";
GRANT ALL ON TABLE "public"."birth_record" TO "authenticated";
GRANT ALL ON TABLE "public"."birth_record" TO "service_role";



GRANT ALL ON SEQUENCE "public"."birth_record_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."birth_record_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."birth_record_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."birth_record_phenotype" TO "anon";
GRANT ALL ON TABLE "public"."birth_record_phenotype" TO "authenticated";
GRANT ALL ON TABLE "public"."birth_record_phenotype" TO "service_role";



GRANT ALL ON TABLE "public"."profiles" TO "anon";
GRANT ALL ON TABLE "public"."profiles" TO "authenticated";
GRANT ALL ON TABLE "public"."profiles" TO "service_role";
GRANT SELECT,INSERT,UPDATE ON TABLE "public"."profiles" TO "supabase_auth_admin";
GRANT INSERT ON TABLE "public"."profiles" TO "authenticator";



GRANT ALL ON TABLE "public"."relationship" TO "anon";
GRANT ALL ON TABLE "public"."relationship" TO "authenticated";
GRANT ALL ON TABLE "public"."relationship" TO "service_role";



GRANT ALL ON SEQUENCE "public"."relationship_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."relationship_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."relationship_id_seq" TO "service_role";



GRANT ALL ON TABLE "public"."sheep" TO "anon";
GRANT ALL ON TABLE "public"."sheep" TO "authenticated";
GRANT ALL ON TABLE "public"."sheep" TO "service_role";



GRANT ALL ON TABLE "public"."sheep_distribution" TO "anon";
GRANT ALL ON TABLE "public"."sheep_distribution" TO "authenticated";
GRANT ALL ON TABLE "public"."sheep_distribution" TO "service_role";



GRANT ALL ON TABLE "public"."sheep_genotype" TO "anon";
GRANT ALL ON TABLE "public"."sheep_genotype" TO "authenticated";
GRANT ALL ON TABLE "public"."sheep_genotype" TO "service_role";



GRANT ALL ON SEQUENCE "public"."sheep_id_seq" TO "anon";
GRANT ALL ON SEQUENCE "public"."sheep_id_seq" TO "authenticated";
GRANT ALL ON SEQUENCE "public"."sheep_id_seq" TO "service_role";









ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES  TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES  TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES  TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON SEQUENCES  TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS  TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS  TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS  TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON FUNCTIONS  TO "service_role";






ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES  TO "postgres";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES  TO "anon";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES  TO "authenticated";
ALTER DEFAULT PRIVILEGES FOR ROLE "postgres" IN SCHEMA "public" GRANT ALL ON TABLES  TO "service_role";






























drop extension if exists "pg_net";

CREATE TRIGGER on_auth_user_created AFTER INSERT ON auth.users FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();


