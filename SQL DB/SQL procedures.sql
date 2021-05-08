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

CREATE OR REPLACE procedure get_book(BOOK_ID_V IN NUMBER, HALL_ID_V IN NUMBER, CARD_ID_V IN NUMBER, RETURN_DATE_V IN DATE)
IS
	hallt VARCHAR(40);
	cnt    NUMBER;
	rules  NUMBER;
BEGIN
	select count(*) into cnt from READER_CARD rd where rd.ID = CARD_ID_V;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'We havent this user!');
	end if;

	select COUNT into cnt from HALL_STORAGE hs where hs.BOOK_ID = BOOK_ID_V and hs.HALL_ID = HALL_ID_V;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'We havent this book in this hall!');
	end if;

	select count(*) into cnt from VIOLATIONS v where v.CARD_ID = CARD_ID_V and v.CARD_LOCK_UNTIL >= CURRENT_DATE;

	if cnt > 0 then
		RAISE_APPLICATION_ERROR(-20010, 'This reader under sanctions and cant take book now!');
	end if;

	select HALL_TYPE_ID into hallt from LIBRARY_HALLS where ID = HALL_ID_V;

	if hallt = 'intercol' then
		select count(*) into cnt from MA_ORDER mao where mao.BOOK_ID = BOOK_ID_V and mao.CARD_ID = CARD_ID_V and mao.ORDER_DATE <= CURRENT_DATE and CURRENT_DATE <= mao.RETURN_DATE and mao.TAKEN is NULL;

		if cnt = 0 then
			RAISE_APPLICATION_ERROR(-20010, 'This book was not ordered!');
		end if;

		update MA_ORDER mao SET taken = CURRENT_DATE where BOOK_ID_V = mao.BOOK_ID and mao.CARD_ID = CARD_ID_V and mao.ORDER_DATE <= CURRENT_DATE and CURRENT_DATE <= mao.RETURN_DATE and mao.TAKEN is NULL and ROWNUM = 1;
	elsif hallt = 'abonement' then
		select CAN_TAKE_FOR_TIME into rules from READER_CARD rc, HUMAN h, READER_TYPE rt
                                where CARD_ID_V = rc.ID and h.ID = rc.HUMAN_ID and h.TYPE_ID = rt.ID;

		if rules != 1 then
			RAISE_APPLICATION_ERROR(-20010, 'This reader havent rules for taking away book!');
		end if;
	end if;

	insert into ACCEPTING_RETURNING_BOOKS values (BOOK_ID_V, HALL_ID_V, CARD_ID_V, CURRENT_DATE, RETURN_DATE_V, NULL, 0);
	update HALL_STORAGE hs SET COUNT = COUNT - 1 where hs.HALL_ID = HALL_ID_V and hs.BOOK_ID = BOOK_ID_V;
	delete HALL_STORAGE where COUNT = 0;

	rereg_reader(CARD_ID_V);

	commit;
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure return_book(BOOK_ID_V IN NUMBER, HALL_ID_V IN NUMBER, CARD_ID_V IN NUMBER)
IS
	cnt NUMBER;
BEGIN
    select count(*) into cnt from READER_CARD rd where rd.ID = CARD_ID_V;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'We havent this user!');
	end if;

	select count(*) into cnt from BOOKS hs where hs.ID = BOOK_ID_V;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'We havent this book in library!');
	end if;

	select count(*) into cnt from ACCEPTING_RETURNING_BOOKS arb where arb.BOOK_ID = BOOK_ID_V and arb.HALL_ID = HALL_ID_V and arb.CARD_ID = CARD_ID_V and DATE_ACCEPTING <= CURRENT_DATE and DATE_MUST_RETURNING >= CURRENT_DATE and RETURN_DATE is NULL and LOST_BOOK = 0;

	if cnt = 0 then
		select count(*) into cnt from VIOLATIONS v where v.BOOK_ID = BOOK_ID_V and v.HALL_ID = HALL_ID_V and v.CARD_ID = CARD_ID_V and v.VIOLATION_DATE <= CURRENT_DATE and v.IS_CLOSED = 0;

		if cnt != 0 then
			RAISE_APPLICATION_ERROR(-20010, 'The book is either already considered lost, close the violation issue!');
		else
			select count(*) into cnt from ACCEPTING_RETURNING_BOOKS arb where arb.BOOK_ID = BOOK_ID_V and arb.HALL_ID != HALL_ID_V and arb.CARD_ID = CARD_ID_V and DATE_ACCEPTING <= CURRENT_DATE and DATE_MUST_RETURNING >= CURRENT_DATE and RETURN_DATE is NULL and LOST_BOOK = 0;

			if cnt > 0 then
				RAISE_APPLICATION_ERROR(-20010, 'Hand over the book in the department where it was issued!');
			else
				RAISE_APPLICATION_ERROR(-20010, 'This book didnt give up!');
			end if;
		end if;
	end if;

    if HALL_ID_V != 3 then
	    update HALL_STORAGE hs SET COUNT = COUNT + 1 where hs.HALL_ID = HALL_ID_V and hs.BOOK_ID = BOOK_ID_V;
	end if;
	update MA_ORDER mao SET RETURN_DATE = CURRENT_DATE, RETURN_STATE = 1 where BOOK_ID_V = mao.BOOK_ID and mao.CARD_ID = CARD_ID_V and mao.ORDER_DATE <= CURRENT_DATE and mao.TAKEN is not NULL and RETURN_STATE = 0 and ROWNUM = 1;
	update ACCEPTING_RETURNING_BOOKS arb set RETURN_DATE = CURRENT_DATE where arb.BOOK_ID = BOOK_ID_V and arb.HALL_ID = HALL_ID_V and arb.CARD_ID = CARD_ID_V and DATE_ACCEPTING <= CURRENT_DATE and DATE_MUST_RETURNING >= CURRENT_DATE and RETURN_DATE is NULL and LOST_BOOK = 0 and ROWNUM = 1;

	rereg_reader(CARD_ID_V);

	commit;
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure open_violation(BOOK_ID_V IN NUMBER, HALL_ID_V IN NUMBER, CARD_ID_V IN NUMBER, TYPE_V IN VARCHAR2, LOST_DATE IN DATE, MONETARY_FINE_V IN NUMBER, CARD_LOCK_UNTIL_V IN DATE)
IS
	cnt NUMBER;
BEGIN
    select count(*) into cnt from READER_CARD rd where rd.ID = CARD_ID_V;

    if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'We havent this user!');
	end if;

	select count(*) into cnt from BOOKS hs where hs.ID = BOOK_ID_V;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'We havent this book in library!');
	end if;

	select count(*) into cnt from ACCEPTING_RETURNING_BOOKS arb where arb.BOOK_ID = BOOK_ID_V and arb.HALL_ID = HALL_ID_V and arb.CARD_ID = CARD_ID_V and arb.DATE_ACCEPTING <= LOST_DATE and arb.DATE_MUST_RETURNING >= LOST_DATE;

	if cnt = 0 then
        RAISE_APPLICATION_ERROR(-20010, 'User cant lost or corrupt anything!');
    end if;

	update ACCEPTING_RETURNING_BOOKS arb set LOST_BOOK = 1 where arb.BOOK_ID = BOOK_ID_V and arb.HALL_ID = HALL_ID_V and arb.CARD_ID = CARD_ID_V and arb.DATE_ACCEPTING <= LOST_DATE and arb.DATE_MUST_RETURNING >= LOST_DATE and ROWNUM = 1;
    insert into VIOLATIONS values (BOOK_ID_V, CARD_ID_V, HALL_ID_V, LOST_DATE, TYPE_V, MONETARY_FINE_V, CARD_LOCK_UNTIL_V, 0);

    rereg_reader(CARD_ID_V);
	commit;
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure close_violation(BOOK_ID_V IN NUMBER, HALL_ID_V IN NUMBER, CARD_ID_V IN NUMBER, VIOLATION_DATE_V in DATE, BOOK_RETURNED IN NUMBER)
IS
	cnt NUMBER;
BEGIN
	select count(*) into cnt from VIOLATIONS v where v.BOOK_ID = BOOK_ID_V and v.HALL_ID = HALL_ID_V and v.CARD_ID = CARD_ID_V and v.VIOLATION_DATE = VIOLATION_DATE_V;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'we havent this violation in data base!');
	end if;	
	
	update VIOLATIONS v set IS_CLOSED = 1 where v.BOOK_ID = BOOK_ID_V and v.HALL_ID = HALL_ID_V and v.CARD_ID = CARD_ID_V and v.VIOLATION_DATE = VIOLATION_DATE_V;
	update MA_ORDER mao set RETURN_DATE = CURRENT_DATE, RETURN_STATE = 1 where BOOK_ID_V = mao.BOOK_ID and mao.CARD_ID = CARD_ID_V and mao.TAKEN is not NULL and RETURN_STATE = 0 and ROWNUM = 1;
	
	if BOOK_RETURNED = 1 and HALL_ID_V != 3 then
		update HALL_STORAGE hs SET COUNT = COUNT + 1 where hs.HALL_ID = HALL_ID_V and hs.BOOK_ID = BOOK_ID_V;
	end if;		

	rereg_reader(CARD_ID_V);

	commit;
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

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure add_book_to_hall(BOOK_ID_V IN NUMBER, HALL_ID_V IN NUMBER, COUNT_V IN NUMBER)
IS
	cnt NUMBER;
BEGIN
	select count(*) into cnt from HALL_STORAGE where HALL_ID = HALL_ID_V and BOOK_ID = BOOK_ID_V;

	if cnt = 0 then
		insert into HALL_STORAGE values (HALL_ID_V, BOOK_ID_V, COUNT_V);
	else
	    update HALL_STORAGE set COUNT = COUNT + COUNT_V where BOOK_ID = BOOK_ID_V and HALL_ID = HALL_ID_V;
	end if;

	insert into BOOK_ACCOUNTING values (BOOK_ID_V, HALL_ID_V, CURRENT_DATE, 'add', COUNT_V);

	commit;
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure remove_book_from_hall(BOOK_ID_V IN NUMBER, HALL_ID_V IN NUMBER, COUNT_V IN NUMBER, DELETE_ALL IN NUMBER)
IS
	cnt NUMBER;
BEGIN
	select count(*) into cnt from HALL_STORAGE where HALL_ID = HALL_ID_V and BOOK_ID = BOOK_ID_V;

	if cnt = 0 then
		RAISE_APPLICATION_ERROR(-20010, 'we havent this book in storage!');
	else
	    select "COUNT" into cnt from HALL_STORAGE where HALL_ID = HALL_ID_V and BOOK_ID = BOOK_ID_V;

	    if cnt < COUNT_V and DELETE_ALL = 0 then
            RAISE_APPLICATION_ERROR(-20010, 'There are fewer books in the storage than you want to write out!');
        end if;

	    if DELETE_ALL > 0 then
            delete from HALL_STORAGE where HALL_ID = HALL_ID_V and BOOK_ID = BOOK_ID_V;
            insert into BOOK_ACCOUNTING values (BOOK_ID_V, HALL_ID_V, CURRENT_DATE, 'del', cnt);
        else
	        update HALL_STORAGE set COUNT = COUNT - COUNT_V where BOOK_ID = BOOK_ID_V and HALL_ID = HALL_ID_V;
	        insert into BOOK_ACCOUNTING values (BOOK_ID_V, HALL_ID_V, CURRENT_DATE, 'del', COUNT_V);
        end if;
	end if;
	commit;
END;

------------------------------------------------------------------------------------------------

CREATE OR REPLACE procedure order_book(BOOK_ID_V IN NUMBER, CARD_ID_V IN NUMBER, RETURN_DATE_V IN DATE)
IS
	cnt NUMBER;
BEGIN
	insert into MA_ORDER values (BOOK_ID_V, CARD_ID_V, CURRENT_DATE, RETURN_DATE_V, NULL, 0);

	select COUNT(*) into cnt from HALL_STORAGE where BOOK_ID = BOOK_ID_V and HALL_ID = 3;

	if cnt = 0 then
	    insert into HALL_STORAGE values (3, BOOK_ID_V, 1);
	else
	    update HALL_STORAGE set COUNT = COUNT + 1 where HALL_ID = 3 and BOOK_ID = BOOK_ID_V;
	end if;

	commit;
END;