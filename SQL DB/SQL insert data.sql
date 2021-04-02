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