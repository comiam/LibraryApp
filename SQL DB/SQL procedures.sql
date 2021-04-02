CREATE OR REPLACE procedure rereg_reader(CARD_ID IN NUMBER)
IS
	rdate DATE;
BEGIN
	select REWRITE_DATE into rdate from READER_CARD where ID = CARD_ID;
	
	if rdate < trunc(sysdate, 'YEAR') then
		update READER_CARD set REWRITE_DATE = CURRENT_DATE where ID = CARD_ID;
	end if;

	commit;
EXCEPTION
WHEN OTHERS THEN
   raise_application_error(-20010,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure get_book(BOOK_ID IN NUMBER, HALL_ID IN NUMBER, CARD_ID IN NUMBER, RETURN_DATE IN DATE)
IS
	hallt VARCHAR(40);
	cnt    NUMBER;
	rules  NUMBER;
BEGIN
	select COUNT into cnt from HALL_STORAGE hs where hs.BOOK_ID = BOOK_ID and hs.HALL_ID = HALL_ID;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'We havent this book in this hall!');
	end if;

	select count(*) into cnt from VIOLATIONS v where v.CARD_ID = CARD_ID and v.CARD_LOCK_UNTIL <= CURRENT_DATE;

	if cnt > 0 then
		RAISE_APPLICATION_ERROR(-20010, 'This reader under sanctions and cant take book now!');
	end if;

	select HALL_TYPE_ID into hallt from LIBRARY_HALLS where ID = HALL_ID;

	if hallt = 'intercol' then
		select count(*) into cnt from MA_ORDER mao where mao.BOOK_ID = BOOK_ID and mao.CARD_ID = CARD_ID and mao.ORDER_DATE <= CURRENT_DATE and CURRENT_DATE <= mao.RETURN_DATE and mao.TAKEN is NULL;

		if cnt > 0 then
			RAISE_APPLICATION_ERROR(-20010, 'This book was not ordered!');
		end if;

		update MA_ORDER mao SET taken = CURRENT_DATE where BOOK_ID = mao.BOOK_ID and mao.CARD_ID = CARD_ID;
	elsif hallt = 'abonement' then
		select CAN_TAKE_FOR_TIME into rules from READER_CARD rc, HUMAN h, READER_TYPE rt
                                where CARD_ID = rc.ID and h.ID = rc.HUMAN_ID and h.TYPE_ID = rt.ID;

		if rules != 1 then
			RAISE_APPLICATION_ERROR(-20010, 'This reader havent rules for taking away book!');
		end if;
	end if;			

	insert into ACCEPTING_RETURNING_BOOKS values (BOOK_ID, HALL_ID, CARD_ID, CURRENT_DATE, RETURN_DATE, NULL, 0);

	update HALL_STORAGE hs SET COUNT = COUNT - 1 where hs.HALL_ID = HALL_ID and hs.BOOK_ID = BOOK_ID;

	rereg_reader(CARD_ID);

	commit;
EXCEPTION
WHEN OTHERS THEN
   raise_application_error(-20010,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure return_book(BOOK_ID IN NUMBER, HALL_ID IN NUMBER, CARD_ID IN NUMBER)
IS
	cnt NUMBER;
BEGIN
	select count(*) into cnt from ACCEPTING_RETURNING_BOOKS arb where arb.BOOK_ID = BOOK_ID and arb.HALL_ID = HALL_ID and arb.CARD_ID = CARD_ID and DATE_ACCEPTING <= CURRENT_DATE and DATE_MUST_RETURNING <= CURRENT_DATE and RETURN_DATE is NULL and LOST_BOOK = 0;

	if cnt = 0 then
		select count(*) into cnt from VIOLATIONS v where v.BOOK_ID = BOOK_ID and v.HALL_ID = HALL_ID and v.CARD_ID = CARD_ID and v.VIOLATION_DATE <= CURRENT_DATE and v.IS_CLOSED = 0;

		if cnt != 0 then
			RAISE_APPLICATION_ERROR(-20010, 'The book is either already considered lost, close the violation issue!');
		else
			select count(*) into cnt from ACCEPTING_RETURNING_BOOKS arb where arb.BOOK_ID = BOOK_ID and arb.HALL_ID != HALL_ID and arb.CARD_ID = CARD_ID and DATE_ACCEPTING <= CURRENT_DATE and DATE_MUST_RETURNING <= CURRENT_DATE and RETURN_DATE is NULL and LOST_BOOK = 0;

			if cnt != 0 then
				RAISE_APPLICATION_ERROR(-20010, 'Hand over the book in the department where it was issued!');
			else
				RAISE_APPLICATION_ERROR(-20010, 'Book it is not registered in the library!');
			end if;
		end if;
	end if;

	update HALL_STORAGE hs SET COUNT = COUNT + 1 where hs.HALL_ID = HALL_ID and hs.BOOK_ID = BOOK_ID;

	update MA_ORDER mao SET RETURN_STATE = 1, RETURN_DATE = CURRENT_DATE where BOOK_ID = mao.BOOK_ID and mao.CARD_ID = CARD_ID;

	update ACCEPTING_RETURNING_BOOKS arb set RETURN_DATE = CURRENT_DATE where arb.BOOK_ID = BOOK_ID and arb.HALL_ID = HALL_ID and arb.CARD_ID = CARD_ID and DATE_ACCEPTING <= CURRENT_DATE and DATE_MUST_RETURNING <= CURRENT_DATE and RETURN_DATE is NULL and LOST_BOOK = 0;

	rereg_reader(CARD_ID);

	commit;
EXCEPTION
WHEN OTHERS THEN
   raise_application_error(-20010,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure close_violation(BOOK_ID IN NUMBER, HALL_ID IN NUMBER, CARD_ID IN NUMBER, VIOLATION_DATE in DATE)
IS
	cnt NUMBER;
BEGIN
	select count(*) into cnt from VIOLATIONS v where v.BOOK_ID = BOOK_ID and v.HALL_ID = HALL_ID and v.CARD_ID = CARD_ID and v.VIOLATION_DATE = VIOLATION_DATE;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'we havent this violation in data base!');
	end if;	
	
	update VIOLATIONS v set IS_CLOSED = 1 where v.BOOK_ID = BOOK_ID and v.HALL_ID = HALL_ID and v.CARD_ID = CARD_ID and v.VIOLATION_DATE = VIOLATION_DATE;

	rereg_reader(CARD_ID);

	commit;
EXCEPTION
WHEN OTHERS THEN
   raise_application_error(-20010,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure update_lateret_violations
IS
	CURSOR inv is select * from ACCEPTING_RETURNING_BOOKS where DATE_MUST_RETURNING < CURRENT_DATE and RETURN_DATE is NULL;
BEGIN
	
   FOR rec in inv
   LOOP
      insert into VIOLATIONS values (rec.BOOK_ID, rec.CARD_ID, rec.HALL_ID, CURRENT_DATE, 'lateret', 1000, NULL, 0);
   END LOOP;

   commit;
EXCEPTION
WHEN OTHERS THEN
   raise_application_error(-20010,'An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
END;