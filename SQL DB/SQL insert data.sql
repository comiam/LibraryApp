--READER TYPE
insert into READER_TYPE values (0, 'student', 1);
insert into READER_TYPE values (0, 'teacher', 1);
insert into READER_TYPE values (0, 'assistant', 1);
insert into READER_TYPE values (0, 'sfpk', 0);
insert into READER_TYPE values (0, 'spo', 0);
insert into READER_TYPE values (0, 'abitura', 0);

--FACULTY
insert into FACULTY values ('FIT');
insert into FACULTY values ('FIJA');
insert into FACULTY values ('FizFak');
insert into FACULTY values ('MMF');
insert into FACULTY values ('FEN');

--GROUPS	
insert into GROUPS values (18209);
insert into GROUPS values (18210);
insert into GROUPS values (20301);
insert into GROUPS values (20303);
insert into GROUPS values (19822);
insert into GROUPS values (19823);
insert into GROUPS values (19111);
insert into GROUPS values (19143);
insert into GROUPS values (19404);	
insert into GROUPS values (19405);	

--TEACHER_GRADE
insert into TEACHER_GRADE values ('docent');
insert into TEACHER_GRADE values ('professor');
insert into TEACHER_GRADE values ('akademic');

--TEACHER_POST
insert into TEACHER_POST values ('kandidat');
insert into TEACHER_POST values ('doctor');

--SUBJECTS 
insert into SUBJECTS values ('chemistry');
insert into SUBJECTS values ('math');
insert into SUBJECTS values ('physics');
insert into SUBJECTS values ('english');
insert into SUBJECTS values ('spanish');
insert into SUBJECTS values ('programming');	

--DEPARTMENT 
insert into DEPARTMENT values ('programming');
insert into DEPARTMENT values ('physics technologies');
insert into DEPARTMENT values ('enternational speaking');
insert into DEPARTMENT values ('fundamental math');
insert into DEPARTMENT values ('quantum computing');

--VIOLATION_TYPES
insert into VIOLATION_TYPES values ('lost');
insert into VIOLATION_TYPES values ('lateret');

--HALL_TYPE
insert into HALL_TYPE values ('intercol');
insert into HALL_TYPE values ('readhall');
insert into HALL_TYPE values ('abonement');	

--Physics
insert into BOOKS values (0, 'You are joking, of course, Mr. Feynman!', 'Richard Feynman', to_date('1985', 'yyyy'));
insert into BOOKS values (0, 'Feynman Lectures on Gravity', 'Richard Feynman', to_date('2000', 'yyyy'));
insert into BOOKS values (0, 'Feynman Lectures on Physics', 'Richard Feynman', to_date('1967', 'yyyy'));

--Math
insert into BOOKS values (0, 'Collection of problems in linear algebra', 'Igor Vladimirovich Proscuryakov', to_date('2010', 'yyyy'));
insert into BOOKS values (0, 'Collection of problems in mathematical analysis', 'Boris Pavlovich Demidovich', to_date('1995', 'yyyy'));
insert into BOOKS values (0, 'Diff. geom. and Lie algebras and their app-s in field theory', 'Igor Pavlovich Volobuev', to_date('1998', 'yyyy'));

--Chemistry
insert into BOOKS values (0, 'Organic chemistry. All volumes', 'Traven Valery Fedorovich', to_date('2015', 'yyyy'));
insert into BOOKS values (0, 'Physical chemistry course. All volumes', 'Yakov Ivanovich Gerasimov', to_date('1964', 'yyyy'));
insert into BOOKS values (0, 'Quantum biochemistry for chemists and biologists', 'Yakov Ladik', to_date('1975', 'yyyy'));

--Langs
insert into BOOKS values (0, 'The word is alive and the word is dead', 'Nora Gal', to_date('1972', 'yyyy'));
insert into BOOKS values (0, 'Theory of language. Introductory course', 'Boris Ustinovich Norman', to_date('2003', 'yyyy'));
insert into BOOKS values (0, 'Introduction to the Formal Language of Linguistics', 'Yuri Konstantinovich Lekomtsev', to_date('1983', 'yyyy'));

--Programming
insert into BOOKS values (0, 'C++ for beginners', 'Herbert Schildt', to_date('2013', 'yyyy'));
insert into BOOKS values (0, 'Java Philosophy', 'Eckel Bruce', to_date('2019', 'yyyy'));
insert into BOOKS values (0, 'Modern operating systems', 'Andrew Tanenbaum', to_date('2015', 'yyyy'));

--LIBRARY_HALLS
insert into LIBRARY_HALLS values(0, 'abonement');
insert into LIBRARY_HALLS values(1, 'abonement');
insert into LIBRARY_HALLS values(2, 'intercol');
insert into LIBRARY_HALLS values(3, 'readhall');
insert into LIBRARY_HALLS values(4, 'readhall');	

--HALL STORAGE - test_data
insert into HALL_STORAGE values(4, 1, 11);
insert into HALL_STORAGE values(4, 2, 18);
insert into HALL_STORAGE values(4, 3, 12);
insert into HALL_STORAGE values(4, 4, 14);
insert into HALL_STORAGE values(4, 5, 16);
insert into HALL_STORAGE values(4, 6, 12);
insert into HALL_STORAGE values(4, 7, 23);
insert into HALL_STORAGE values(4, 8, 26);
insert into HALL_STORAGE values(4, 9, 13);
insert into HALL_STORAGE values(4, 10, 9);
insert into HALL_STORAGE values(4, 11, 18);
insert into HALL_STORAGE values(4, 12, 15);
insert into HALL_STORAGE values(4, 14, 34);
insert into HALL_STORAGE values(4, 15, 45);
insert into HALL_STORAGE values(4, 16, 23);

insert into HALL_STORAGE values(5, 1, 23);
insert into HALL_STORAGE values(5, 2, 34);
insert into HALL_STORAGE values(5, 3, 17);
insert into HALL_STORAGE values(5, 4, 5);
insert into HALL_STORAGE values(5, 5, 1);
insert into HALL_STORAGE values(5, 6, 6);
insert into HALL_STORAGE values(5, 7, 12);
insert into HALL_STORAGE values(5, 8, 20);
insert into HALL_STORAGE values(5, 9, 8);
insert into HALL_STORAGE values(5, 10, 15);
insert into HALL_STORAGE values(5, 11, 34);
insert into HALL_STORAGE values(5, 12, 23);
insert into HALL_STORAGE values(5, 14, 14);
insert into HALL_STORAGE values(5, 15, 23);
insert into HALL_STORAGE values(5, 16, 45);	

insert into HALL_STORAGE values(1, 1, 56);
insert into HALL_STORAGE values(1, 2, 89);
insert into HALL_STORAGE values(1, 3, 56);
insert into HALL_STORAGE values(1, 4, 34);
insert into HALL_STORAGE values(1, 5, 123);
insert into HALL_STORAGE values(1, 6, 67);
insert into HALL_STORAGE values(1, 7, 34);
insert into HALL_STORAGE values(1, 8, 53);
insert into HALL_STORAGE values(1, 9, 74);
insert into HALL_STORAGE values(1, 10, 23);
insert into HALL_STORAGE values(1, 11, 78);
insert into HALL_STORAGE values(1, 12, 56);
insert into HALL_STORAGE values(1, 14, 67);
insert into HALL_STORAGE values(1, 15, 54);
insert into HALL_STORAGE values(1, 16, 34);	

insert into HALL_STORAGE values(2, 1, 78);
insert into HALL_STORAGE values(2, 2, 45);
insert into HALL_STORAGE values(2, 3, 45);
insert into HALL_STORAGE values(2, 4, 53);
insert into HALL_STORAGE values(2, 5, 96);
insert into HALL_STORAGE values(2, 6, 23);
insert into HALL_STORAGE values(2, 7, 43);
insert into HALL_STORAGE values(2, 8, 123);
insert into HALL_STORAGE values(2, 9, 33);
insert into HALL_STORAGE values(2, 10, 21);
insert into HALL_STORAGE values(2, 11, 67);
insert into HALL_STORAGE values(2, 12, 94);
insert into HALL_STORAGE values(2, 14, 24);
insert into HALL_STORAGE values(2, 15, 35);
insert into HALL_STORAGE values(2, 16, 76);		