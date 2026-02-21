-- =========
-- TYPES
-- =========

CREATE TYPE public.app_food_pending_status AS ENUM (
    'pending',
    'approved',
    'rejected'
);

CREATE TYPE public.food_log_category AS ENUM (
    'breakfast',
    'lunch',
    'dinner',
    'supplement'
);

CREATE TYPE public.food_sort_by AS ENUM (
    'alphabetically',
    'favorite',
    'recently_logged',
    'creation_date'
);

CREATE TYPE public.food_source AS ENUM (
    'user',
    'app'
);

CREATE TYPE public.food_status AS ENUM (
    'present',
    'updated',
    'deleted'
);

CREATE TYPE public.nutrient_type AS ENUM (
    'basic',
    'vitamin',
    'mineral'
);

CREATE TYPE public.serving_unit AS ENUM (
    'g',
    'mg',
    'mcg',
    'ml',
    'oz',
    'kcal'
);

CREATE TYPE public.user_currency_transaction_type AS ENUM (
    'food_approval',
    'redeem',
    'food_logged'
);

CREATE TYPE public.user_type AS ENUM (
    'user',
    'contributor',
    'admin'
);

-- =========
-- TABLES
-- =========

CREATE TABLE public.users (
                              id uuid NOT NULL,
                              name character varying(24) NOT NULL,
                              email character varying(255) NOT NULL,
                              password_hash character varying(255) NOT NULL,
                              is_premium boolean DEFAULT false NOT NULL,
                              created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              type public.user_type DEFAULT 'user'::public.user_type NOT NULL,
                              PRIMARY KEY (id)
);

CREATE TABLE public.nutrients (
                                  id integer NOT NULL,
                                  name character varying(50) NOT NULL,
                                  symbol character varying(4),
                                  unit public.serving_unit NOT NULL,
                                  type public.nutrient_type NOT NULL,
                                  is_premium boolean DEFAULT false NOT NULL,
                                  PRIMARY KEY (id),
                                  CONSTRAINT nutrients_name_check CHECK ((length((name)::text) > 0))
);

CREATE SEQUENCE public.nutrients_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.nutrients_id_seq OWNED BY public.nutrients.id;
ALTER TABLE ONLY public.nutrients ALTER COLUMN id SET DEFAULT nextval('public.nutrients_id_seq'::regclass);

CREATE TABLE public.refresh_tokens (
                                       id uuid NOT NULL,
                                       user_id uuid NOT NULL,
                                       device_name character varying(255),
                                       hash character varying(255) NOT NULL,
                                       created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       expires_at timestamp with time zone NOT NULL,
                                       last_used_at timestamp with time zone,
                                       revoked_at timestamp without time zone,
                                       PRIMARY KEY (id),
                                       FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.password_reset_tokens (
                                              id uuid NOT NULL,
                                              user_id uuid NOT NULL,
                                              token_hash character varying(255) NOT NULL,
                                              used boolean DEFAULT false,
                                              expires_at timestamp with time zone NOT NULL,
                                              created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                              PRIMARY KEY (id),
                                              FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.user_wallets (
                                     user_id uuid NOT NULL,
                                     amount numeric(12,2) DEFAULT 0.00 NOT NULL,
                                     PRIMARY KEY (user_id),
                                     FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.user_currency_transactions (
                                                   id integer NOT NULL,
                                                   user_id uuid NOT NULL,
                                                   amount numeric(12,2) NOT NULL,
                                                   transaction_type public.user_currency_transaction_type NOT NULL,
                                                   created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                                   PRIMARY KEY (id),
                                                   FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE SEQUENCE public.user_currency_transactions_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.user_currency_transactions_id_seq OWNED BY public.user_currency_transactions.id;
ALTER TABLE ONLY public.user_currency_transactions ALTER COLUMN id SET DEFAULT nextval('public.user_currency_transactions_id_seq'::regclass);

CREATE TABLE public.user_preferences (
                                         user_id uuid NOT NULL,
                                         food_sort_by public.food_sort_by DEFAULT 'recently_logged'::public.food_sort_by NOT NULL,
                                         PRIMARY KEY (user_id),
                                         FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE TABLE public.user_nutrient_preferences (
                                                  user_id uuid NOT NULL,
                                                  nutrient_id integer NOT NULL,
                                                  goal numeric(12,4),
                                                  hex_color character varying(7),
                                                  PRIMARY KEY (user_id, nutrient_id),
                                                  FOREIGN KEY (user_id) REFERENCES public.users(id),
                                                  FOREIGN KEY (nutrient_id) REFERENCES public.nutrients(id),
                                                  CONSTRAINT user_nutrient_preferences_goal_check CHECK ((goal > (0)::numeric))
);

CREATE TABLE public.user_foods (
                                   id integer NOT NULL,
                                   user_id uuid NOT NULL,
                                   name text NOT NULL,
                                   brand text,
                                   amount_per_serving numeric(12,4) NOT NULL,
                                   serving_unit public.serving_unit NOT NULL,
                                   is_favorite boolean DEFAULT false NOT NULL,
                                   last_logged_at timestamp without time zone,
                                   created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                   PRIMARY KEY (id),
                                   FOREIGN KEY (user_id) REFERENCES public.users(id),
                                   CONSTRAINT user_foods_amount_per_serving_check CHECK ((amount_per_serving > (0)::numeric)),
                                   CONSTRAINT user_foods_brand_check CHECK (((brand IS NULL) OR (length(brand) <= 50))),
                                   CONSTRAINT user_foods_name_check CHECK (((length(name) > 0) AND (length(name) <= 50)))
);

CREATE SEQUENCE public.user_foods_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.user_foods_id_seq OWNED BY public.user_foods.id;
ALTER TABLE ONLY public.user_foods ALTER COLUMN id SET DEFAULT nextval('public.user_foods_id_seq'::regclass);

CREATE TABLE public.user_food_nutrients (
                                            user_food_id integer NOT NULL,
                                            nutrient_id integer NOT NULL,
                                            amount numeric(12,4) NOT NULL,
                                            PRIMARY KEY (user_food_id, nutrient_id),
                                            FOREIGN KEY (user_food_id) REFERENCES public.user_foods(id) ON DELETE CASCADE,
                                            FOREIGN KEY (nutrient_id) REFERENCES public.nutrients(id),
                                            CONSTRAINT user_food_nutrients_amount_check CHECK ((amount > (0)::numeric))
);

CREATE TABLE public.user_food_snapshots (
                                            id integer NOT NULL,
                                            original_food_id integer NOT NULL,
                                            user_id uuid NOT NULL,
                                            name text,
                                            brand text,
                                            amount_per_serving numeric(12,4),
                                            serving_unit public.serving_unit NOT NULL,
                                            food_status public.food_status NOT NULL,
                                            created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                            PRIMARY KEY (id),
                                            FOREIGN KEY (user_id) REFERENCES public.users(id)
);

CREATE SEQUENCE public.user_food_snapshots_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.user_food_snapshots_id_seq OWNED BY public.user_food_snapshots.id;
ALTER TABLE ONLY public.user_food_snapshots ALTER COLUMN id SET DEFAULT nextval('public.user_food_snapshots_id_seq'::regclass);

CREATE TABLE public.user_food_logs (
                                       id integer NOT NULL,
                                       user_id uuid NOT NULL,
                                       food_id integer,
                                       food_snapshot_id integer,
                                       servings numeric(12,4) NOT NULL,
                                       category public.food_log_category NOT NULL,
                                       "time" timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       logged_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                       source public.food_source NOT NULL,
                                       PRIMARY KEY (id),
                                       FOREIGN KEY (user_id) REFERENCES public.users(id),
                                       CONSTRAINT user_food_logs_servings_check CHECK ((servings > (0)::numeric))
);

CREATE SEQUENCE public.user_food_logs_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.user_food_logs_id_seq OWNED BY public.user_food_logs.id;
ALTER TABLE ONLY public.user_food_logs ALTER COLUMN id SET DEFAULT nextval('public.user_food_logs_id_seq'::regclass);

CREATE TABLE public.user_nutrient_intake (
                                             id integer NOT NULL,
                                             user_id uuid NOT NULL,
                                             food_log_id integer NOT NULL,
                                             nutrient_id integer NOT NULL,
                                             intake_amount numeric(12,4) NOT NULL,
                                             PRIMARY KEY (id),
                                             FOREIGN KEY (user_id) REFERENCES public.users(id),
                                             FOREIGN KEY (food_log_id) REFERENCES public.user_food_logs(id),
                                             FOREIGN KEY (nutrient_id) REFERENCES public.nutrients(id),
                                             CONSTRAINT user_nutrient_intake_intake_amount_check CHECK ((intake_amount > (0)::numeric))
);

CREATE SEQUENCE public.user_nutrient_intake_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.user_nutrient_intake_id_seq OWNED BY public.user_nutrient_intake.id;
ALTER TABLE ONLY public.user_nutrient_intake ALTER COLUMN id SET DEFAULT nextval('public.user_nutrient_intake_id_seq'::regclass);

CREATE TABLE public.app_foods (
                                  id integer NOT NULL,
                                  name character varying(50) NOT NULL,
                                  brand character varying(50) NOT NULL,
                                  amount_per_serving numeric(12,4) NOT NULL,
                                  serving_unit public.serving_unit NOT NULL,
                                  created_by uuid,
                                  created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                  PRIMARY KEY (id),
                                  FOREIGN KEY (created_by) REFERENCES public.users(id) ON DELETE SET NULL,
                                  CONSTRAINT app_foods_amount_per_serving_check CHECK ((amount_per_serving > (0)::numeric)),
                                  UNIQUE (name, brand, amount_per_serving, serving_unit)
);

CREATE SEQUENCE public.app_foods_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.app_foods_id_seq OWNED BY public.app_foods.id;
ALTER TABLE ONLY public.app_foods ALTER COLUMN id SET DEFAULT nextval('public.app_foods_id_seq'::regclass);

CREATE TABLE public.app_food_nutrients (
                                           app_food_id integer NOT NULL,
                                           nutrient_id integer NOT NULL,
                                           amount numeric(12,4) NOT NULL,
                                           PRIMARY KEY (app_food_id, nutrient_id),
                                           FOREIGN KEY (app_food_id) REFERENCES public.app_foods(id) ON DELETE CASCADE,
                                           FOREIGN KEY (nutrient_id) REFERENCES public.nutrients(id),
                                           CONSTRAINT app_food_nutrients_amount_check CHECK ((amount >= (0)::numeric))
);

CREATE TABLE public.user_favorite_app_foods (
                                                user_id uuid NOT NULL,
                                                app_food_id integer NOT NULL,
                                                favorited_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                                PRIMARY KEY (user_id, app_food_id),
                                                FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE,
                                                FOREIGN KEY (app_food_id) REFERENCES public.app_foods(id) ON DELETE CASCADE
);

CREATE TABLE public.user_pending_foods (
                                           id integer NOT NULL,
                                           name character varying(50) NOT NULL,
                                           brand character varying(50) NOT NULL,
                                           amount_per_serving numeric(12,4) NOT NULL,
                                           serving_unit public.serving_unit NOT NULL,
                                           created_by uuid NOT NULL,
                                           status public.app_food_pending_status DEFAULT 'pending'::public.app_food_pending_status NOT NULL,
                                           reviewed_by uuid,
                                           reviewed_at timestamp without time zone,
                                           created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                           rejection_reason text,
                                           PRIMARY KEY (id),
                                           FOREIGN KEY (created_by) REFERENCES public.users(id),
                                           FOREIGN KEY (reviewed_by) REFERENCES public.users(id),
                                           CONSTRAINT user_pending_foods_amount_per_serving_check CHECK ((amount_per_serving > (0)::numeric)),
                                           CONSTRAINT user_pending_foods_rejection_reason_check CHECK (((rejection_reason IS NULL) OR (length(rejection_reason) <= 250)))
);

CREATE SEQUENCE public.user_pending_foods_id_seq
    AS integer START WITH 1 INCREMENT BY 1
    NO MINVALUE NO MAXVALUE CACHE 1;

ALTER SEQUENCE public.user_pending_foods_id_seq OWNED BY public.user_pending_foods.id;
ALTER TABLE ONLY public.user_pending_foods ALTER COLUMN id SET DEFAULT nextval('public.user_pending_foods_id_seq'::regclass);

CREATE TABLE public.user_pending_food_nutrients (
                                                    pending_food_id integer NOT NULL,
                                                    nutrient_id integer NOT NULL,
                                                    amount numeric(12,4) NOT NULL,
                                                    PRIMARY KEY (pending_food_id, nutrient_id),
                                                    FOREIGN KEY (pending_food_id) REFERENCES public.user_pending_foods(id) ON DELETE CASCADE,
                                                    FOREIGN KEY (nutrient_id) REFERENCES public.nutrients(id),
                                                    CONSTRAINT user_pending_food_nutrients_amount_check CHECK ((amount > (0)::numeric))
);

-- =========
-- SEED DATA
-- =========

INSERT INTO public.nutrients (id, name, symbol, unit, type, is_premium) VALUES
                                                                            (1,  'Calories',    NULL, 'kcal', 'basic',   false),
                                                                            (2,  'Carbs',       NULL, 'g',    'basic',   false),
                                                                            (3,  'Cholesterol', NULL, 'mg',   'basic',   true),
                                                                            (4,  'Fats',        NULL, 'g',    'basic',   false),
                                                                            (5,  'Fiber',       NULL, 'g',    'basic',   true),
                                                                            (6,  'Protein',     NULL, 'g',    'basic',   false),
                                                                            (7,  'Sodium',      NULL, 'mg',   'basic',   true),
                                                                            (8,  'Sugar',       NULL, 'g',    'basic',   true),
                                                                            (9,  'A',           NULL, 'mcg',  'vitamin', true),
                                                                            (10, 'B12',         NULL, 'mcg',  'vitamin', false),
                                                                            (11, 'C',           NULL, 'mg',   'vitamin', false),
                                                                            (12, 'D',           NULL, 'mcg',  'vitamin', false),
                                                                            (13, 'Calcium',     NULL, 'mg',   'mineral', true),
                                                                            (14, 'Iron',        NULL, 'mg',   'mineral', false),
                                                                            (15, 'Magnesium',   NULL, 'mg',   'mineral', false),
                                                                            (16, 'Potassium',   NULL, 'mg',   'mineral', false);

-- Reset sequence so next insert starts after our seeded ids
SELECT setval('public.nutrients_id_seq', 16);