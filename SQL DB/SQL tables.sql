-- Run scripts in this order

CREATE TABLE  "READER_TYPE" 
   (	"ID" NUMBER NOT NULL ENABLE, 
	"NAME" VARCHAR2(20) NOT NULL ENABLE, 
	"CAN_TAKE_FOR_TIME" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "READER_TYPE_PK" PRIMARY KEY ("ID") ENABLE, 
	 CONSTRAINT "READER_TYPE_UK1" UNIQUE ("NAME") ENABLE
   )
/

CREATE OR REPLACE TRIGGER  "BI_READER_TYPE" 
  before insert on "READER_TYPE"              
  for each row 
begin  
    select "READER_TYPE_SEQ".nextval into :NEW.ID from dual;
end;
/
ALTER TRIGGER  "BI_READER_TYPE" ENABLE
/

--------------------------------------------------------------------------------------

CREATE TABLE  "HUMAN" 
   (	"ID" NUMBER NOT NULL ENABLE, 
	"TYPE_ID" NUMBER NOT NULL ENABLE, 
	"SURNAME" VARCHAR2(50) NOT NULL ENABLE, 
	"FIRST_NAME" VARCHAR2(50) NOT NULL ENABLE, 
	"PATRONYMIC" VARCHAR2(50) NOT NULL ENABLE, 
	 CONSTRAINT "HUMAN_PK" PRIMARY KEY ("ID") ENABLE, 
	 CONSTRAINT "HUMAN_FK" FOREIGN KEY ("TYPE_ID")
	  REFERENCES  "READER_TYPE" ("ID") ON DELETE CASCADE ENABLE
   )
/

CREATE OR REPLACE TRIGGER  "BI_HUMAN" 
  before insert on "HUMAN"               
  for each row  
begin   
    select "HUMAN_SEQ".nextval into :NEW.ID from dual; 
end; 

/
ALTER TRIGGER  "BI_HUMAN" ENABLE
/

--------------------------------------------------------------------------------------

CREATE TABLE  "READER_CARD" 
   (	"ID" NUMBER NOT NULL ENABLE, 
	"HUMAN_ID" NUMBER NOT NULL ENABLE, 
	"REWRITE_DATE" DATE NOT NULL ENABLE, 
	"REG_DATE" DATE NOT NULL ENABLE, 
	 CONSTRAINT "READER_CARD_PK" PRIMARY KEY ("ID") ENABLE, 
	 CONSTRAINT "READER_CARD_FK" FOREIGN KEY ("HUMAN_ID")
	  REFERENCES  "HUMAN" ("ID") ON DELETE CASCADE ENABLE
   )
/

CREATE OR REPLACE TRIGGER  "BI_READER_CARD" 
  before insert on "READER_CARD"               
  for each row  
begin   
    select "READER_CARD_SEQ".nextval into :NEW.ID from dual; 
end; 

/
ALTER TRIGGER  "BI_READER_CARD" ENABLE
/

CREATE OR REPLACE TRIGGER  "RC_DELETE_VIOLATIONS" 
    before delete on READER_CARD
    for each row
declare
    rowcnt number(6);
    rowcntt number(6);
begin
    select count(*) into rowcnt  from VIOLATIONS where CARD_ID = :old.ID and violation_date < CURRENT_DATE and IS_CLOSED = 0;
    select count(*) into rowcntt from ACCEPTING_RETURNING_BOOKS where CARD_ID = :old.ID and DATE_ACCEPTING < CURRENT_DATE and RETURN_DATE is NULL;

    if rowcnt + rowcntt > 0 then
        RAISE_APPLICATION_ERROR(-20010, 'The owner of this card did not return all the books!');
    end if;
end;
/
ALTER TRIGGER  "RC_DELETE_VIOLATIONS" ENABLE
/

--------------------------------------------------------------------------------------

CREATE TABLE  "CARD_ACCOUNTING" 
   (	"CARD_ID" NUMBER NOT NULL ENABLE, 
	"DATE_TIME" DATE NOT NULL ENABLE, 
	"OPERATION" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "CARD_ACCOUNTING_FK" FOREIGN KEY ("CARD_ID")
	  REFERENCES  "READER_CARD" ("ID") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "FACULTY" 
   (	"ID_NAME" VARCHAR2(20) NOT NULL ENABLE, 
	 CONSTRAINT "FACULTY_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "DEPARTMENT" 
   (	"ID_NAME" VARCHAR2(60) NOT NULL ENABLE, 
	 CONSTRAINT "DEPARTMENT_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "GROUPS" 
   (	"ID_NAME" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "GROUPS_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "TEACHER_GRADE" 
   (	"ID_NAME" VARCHAR2(40) NOT NULL ENABLE, 
	 CONSTRAINT "TEACHER_GRADE_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "TEACHER_POST" 
   (	"ID_NAME" VARCHAR2(40) NOT NULL ENABLE, 
	 CONSTRAINT "TEACHER_POST_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "SUBJECTS" 
   (	"ID_NAME" VARCHAR2(50) NOT NULL ENABLE, 
	 CONSTRAINT "SUBJECTS_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "STUDENTS" 
   (	"HUMAN_ID" NUMBER NOT NULL ENABLE, 
	"GROUP_ID" NUMBER, 
	"FACULTY_ID" VARCHAR2(40), 
	"COURCE" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "STUDENTS_UK1" UNIQUE ("HUMAN_ID") ENABLE, 
	 CONSTRAINT "STUDENTS_FK" FOREIGN KEY ("HUMAN_ID")
	  REFERENCES  "HUMAN" ("ID") ON DELETE CASCADE ENABLE, 
	 CONSTRAINT "STUDENTS_FK2" FOREIGN KEY ("GROUP_ID")
	  REFERENCES  "GROUPS" ("ID_NAME") ON DELETE SET NULL ENABLE, 
	 CONSTRAINT "STUDENTS_FK3" FOREIGN KEY ("FACULTY_ID")
	  REFERENCES  "FACULTY" ("ID_NAME") ON DELETE SET NULL ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "TEACHERS" 
   (	"HUMAN_ID" NUMBER NOT NULL ENABLE, 
	"GRADE_ID" VARCHAR2(40), 
	"POST_ID" VARCHAR2(40), 
	"SUBJECT_ID" VARCHAR2(50), 
	"SUBJECT2_ID" VARCHAR2(50), 
	 CONSTRAINT "TEACHERS_FK" FOREIGN KEY ("HUMAN_ID")
	  REFERENCES  "HUMAN" ("ID") ON DELETE CASCADE ENABLE, 
	 CONSTRAINT "TEACHERS_FK2" FOREIGN KEY ("GRADE_ID")
	  REFERENCES  "TEACHER_GRADE" ("ID_NAME") ON DELETE SET NULL ENABLE, 
	 CONSTRAINT "TEACHERS_FK3" FOREIGN KEY ("POST_ID")
	  REFERENCES  "TEACHER_POST" ("ID_NAME") ON DELETE SET NULL ENABLE, 
	 CONSTRAINT "TEACHERS_FK4" FOREIGN KEY ("SUBJECT_ID")
	  REFERENCES  "SUBJECTS" ("ID_NAME") ON DELETE SET NULL ENABLE, 
	 CONSTRAINT "TEACHERS_FK5" FOREIGN KEY ("SUBJECT2_ID")
	  REFERENCES  "SUBJECTS" ("ID_NAME") ON DELETE SET NULL ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "SPO" 
   (	"HUMAN_ID" NUMBER NOT NULL ENABLE, 
	"SUBJECT_ID" VARCHAR2(40), 
	 CONSTRAINT "SPO_FK" FOREIGN KEY ("HUMAN_ID")
	  REFERENCES  "HUMAN" ("ID") ON DELETE CASCADE ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "ASSISTANT" 
   (	"HUMAN_ID" NUMBER NOT NULL ENABLE, 
	"SUBJECT_ID" VARCHAR2(50), 
	 CONSTRAINT "ASSISTANT_FK" FOREIGN KEY ("HUMAN_ID")
	  REFERENCES  "HUMAN" ("ID") ON DELETE CASCADE ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "HALL_TYPE" 
   (	"ID_NAME" VARCHAR2(40) NOT NULL ENABLE, 
	 CONSTRAINT "HALL_TYPE_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "LIBRARY_HALLS" 
   (	"ID" NUMBER NOT NULL ENABLE, 
	"HALL_TYPE_ID" VARCHAR2(40), 
	 CONSTRAINT "LIBRARY_HALLS_PK" PRIMARY KEY ("ID") ENABLE, 
	 CONSTRAINT "LIBRARY_HALLS_FK" FOREIGN KEY ("HALL_TYPE_ID")
	  REFERENCES  "HALL_TYPE" ("ID_NAME") ON DELETE SET NULL ENABLE
   )
/

CREATE OR REPLACE TRIGGER  "BI_LIBRARY_HALLS" 
  before insert on "LIBRARY_HALLS"               
  for each row  
begin   
    select "LIBRARY_HALLS_SEQ".nextval into :NEW.ID from dual; 
end; 

/
ALTER TRIGGER  "BI_LIBRARY_HALLS" ENABLE
/

--------------------------------------------------------------------------------------

CREATE TABLE  "BOOKS" 
   (	"ID" NUMBER NOT NULL ENABLE, 
	"NAME" VARCHAR2(60) NOT NULL ENABLE, 
	"AUTHOR" VARCHAR2(60) NOT NULL ENABLE, 
	"YEAR_OF_PUBL" DATE NOT NULL ENABLE, 
	"COST" NUMBER, 
	 CONSTRAINT "BOOKS_PK" PRIMARY KEY ("ID") ENABLE, 
	 CONSTRAINT "BOOKS_UK2" UNIQUE ("NAME", "AUTHOR", "YEAR_OF_PUBL") ENABLE
   )
/

CREATE OR REPLACE TRIGGER  "BI_BOOKS" 
  before insert on "BOOKS"               
  for each row  
begin   
    select "BOOKS_SEQ".nextval into :NEW.ID from dual; 
end; 

/
ALTER TRIGGER  "BI_BOOKS" ENABLE
/

CREATE OR REPLACE TRIGGER  "BOOK_DELETES" 
    before delete on BOOKS
    for each row
declare
    rowcnt number(6);
    rowcntt number(6);
begin
    select count(*) into rowcnt  from VIOLATIONS where BOOK_ID = :old.ID and violation_date <= CURRENT_DATE and IS_CLOSED = 0;
    select count(*) into rowcntt from ACCEPTING_RETURNING_BOOKS where BOOK_ID = :old.ID and DATE_ACCEPTING <= CURRENT_DATE and RETURN_DATE is NULL;

    if rowcnt + rowcntt > 0 then
        RAISE_APPLICATION_ERROR(-20010, 'Not all the books were returned to the library!');
    end if;
end;
/
ALTER TRIGGER  "BOOK_DELETES" ENABLE
/

--------------------------------------------------------------------------------------

CREATE TABLE  "HALL_STORAGE" 
   (	"HALL_ID" NUMBER, 
	"BOOK_ID" NUMBER NOT NULL ENABLE, 
	"COUNT" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "HALL_STORAGE_UK1" UNIQUE ("HALL_ID", "BOOK_ID") ENABLE, 
	 CONSTRAINT "HALL_STORAGE_FK" FOREIGN KEY ("HALL_ID")
	  REFERENCES  "LIBRARY_HALLS" ("ID") ON DELETE SET NULL ENABLE, 
	 CONSTRAINT "HALL_STORAGE_FK2" FOREIGN KEY ("BOOK_ID")
	  REFERENCES  "BOOKS" ("ID") ON DELETE CASCADE ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "BOOK_ACCOUNTING" 
   (	"BOOK_ID" NUMBER NOT NULL ENABLE, 
	"HALL_ID" NUMBER NOT NULL ENABLE, 
	"TIME" DATE NOT NULL ENABLE, 
	"OPERATION" VARCHAR2(5) NOT NULL ENABLE, 
	"COUNT" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "BOOK_ACCOUNTING_UK1" UNIQUE ("BOOK_ID", "HALL_ID", "TIME", "OPERATION") ENABLE, 
	 CONSTRAINT "BOOK_ACCOUNTING_FK" FOREIGN KEY ("BOOK_ID")
	  REFERENCES  "BOOKS" ("ID") ENABLE, 
	 CONSTRAINT "BOOK_ACCOUNTING_FK2" FOREIGN KEY ("HALL_ID")
	  REFERENCES  "LIBRARY_HALLS" ("ID") ENABLE
   )
/


--------------------------------------------------------------------------------------

CREATE TABLE  "ACCEPTING_RETURNING_BOOKS" 
   (	"BOOK_ID" NUMBER NOT NULL ENABLE, 
	"HALL_ID" NUMBER NOT NULL ENABLE, 
	"CARD_ID" NUMBER NOT NULL ENABLE, 
	"DATE_ACCEPTING" DATE NOT NULL ENABLE, 
	"DATE_MUST_RETURNING" DATE NOT NULL ENABLE, 
	"RETURN_DATE" DATE, 
	"LOST_BOOK" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "ACCEPTING_RETURNING_BOOKS_UK1" UNIQUE ("BOOK_ID", "HALL_ID", "CARD_ID", "DATE_ACCEPTING") ENABLE, 
	 CONSTRAINT "ACCEPTING_RETURNING_BOOKS_FK" FOREIGN KEY ("BOOK_ID")
	  REFERENCES  "BOOKS" ("ID") ENABLE, 
	 CONSTRAINT "ACCEPTING_RETURNING_BOOKS_FK2" FOREIGN KEY ("HALL_ID")
	  REFERENCES  "LIBRARY_HALLS" ("ID") ENABLE, 
	 CONSTRAINT "ACCEPTING_RETURNING_BOOKS_FK3" FOREIGN KEY ("CARD_ID")
	  REFERENCES  "READER_CARD" ("ID") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "MA_ORDER" 
   (	"BOOK_ID" NUMBER, 
	"CARD_ID" NUMBER, 
	"ORDER_DATE" DATE, 
	"RETURN_DATE" DATE, 
	"TAKEN" DATE, 
	"RETURN_STATE" NUMBER NOT NULL ENABLE, 
	 CONSTRAINT "MA_ORDER_UK1" UNIQUE ("BOOK_ID", "CARD_ID", "ORDER_DATE", "RETURN_DATE", "TAKEN") ENABLE, 
	 CONSTRAINT "MA_ORDER_FK" FOREIGN KEY ("BOOK_ID")
	  REFERENCES  "BOOKS" ("ID") ENABLE, 
	 CONSTRAINT "MA_ORDER_FK2" FOREIGN KEY ("CARD_ID")
	  REFERENCES  "READER_CARD" ("ID") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "VIOLATION_TYPES" 
   (	"ID_NAME" VARCHAR2(40) NOT NULL ENABLE, 
	 CONSTRAINT "VIOLATION_TYPES_UK1" UNIQUE ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "VIOLATIONS" 
   (	"BOOK_ID" NUMBER NOT NULL ENABLE, 
	"CARD_ID" NUMBER NOT NULL ENABLE, 
	"HALL_ID" NUMBER NOT NULL ENABLE, 
	"VIOLATION_DATE" DATE NOT NULL ENABLE, 
	"VIOLATION_TYPE" VARCHAR2(40) NOT NULL ENABLE, 
	"MONETARY_FINE" NUMBER, 
	"CARD_LOCK_UNTIL" DATE, 
	"IS_CLOSED" NUMBER, 
	 CONSTRAINT "VIOLATIONS_UK1" UNIQUE ("BOOK_ID", "CARD_ID", "HALL_ID", "VIOLATION_DATE", "VIOLATION_TYPE") ENABLE, 
	 CONSTRAINT "VIOLATIONS_FK" FOREIGN KEY ("BOOK_ID")
	  REFERENCES  "BOOKS" ("ID") ENABLE, 
	 CONSTRAINT "VIOLATIONS_FK3" FOREIGN KEY ("CARD_ID")
	  REFERENCES  "READER_CARD" ("ID") ENABLE, 
	 CONSTRAINT "VIOLATIONS_FK2" FOREIGN KEY ("HALL_ID")
	  REFERENCES  "LIBRARY_HALLS" ("ID") ENABLE, 
	 CONSTRAINT "VIOLATIONS_FK4" FOREIGN KEY ("VIOLATION_TYPE")
	  REFERENCES  "VIOLATION_TYPES" ("ID_NAME") ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "ADMIN_PASSWORDS"
   (	"ID" VARCHAR2(20) NOT NULL ENABLE,
	"PASSW" VARCHAR2(32) NOT NULL ENABLE,
	 CONSTRAINT "ADMIN_PASSWORDS_CK1" CHECK (LENGTH(PASSW) > 0) ENABLE
   )
/

--------------------------------------------------------------------------------------

CREATE TABLE  "PASSWORDS"
   (	"ID" NUMBER NOT NULL ENABLE,
	"PASSW" VARCHAR2(32) NOT NULL ENABLE,
	 CONSTRAINT "PASSWORDS_CK1" CHECK (LENGTH(PASSW) > 0) ENABLE,
	 CONSTRAINT "PASSWORDS_FK" FOREIGN KEY ("ID")
	  REFERENCES  "READER_CARD" ("ID") ON DELETE CASCADE ENABLE
   )
/