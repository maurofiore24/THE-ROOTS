package com.example.data

import android.util.Log

class DynastySeeder(private val dao: FamilyMemberDao) {

    suspend fun seedDynasty(dynastyKey: String) {
        dao.deleteAll()
        when (dynastyKey) {
            "nemanjici" -> seedNemanjici()
            "tudor" -> seedTudors()
            "romanov" -> seedRomanovs()
            "habsburg" -> seedHabsburgs()
            "bourbon" -> seedBourbons()
            "osman" -> seedOttomans()
            "medici" -> seedMedici()
            "yamato" -> seedYamato()
            "julioclaudian" -> seedJulioClaudian()
            "bonaparte" -> seedBonapartes()
            else -> seedMarkovicDemo()
        }
    }

    private suspend fun seedMarkovicDemo() {
        val f1 = dao.insertMember(FamilyMember(
            firstName = "Čedomir || Čedomir || Чедомир || Čedomir || Čedomir",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "MALE", birthDate = "15.08.1912",
            birthPlace = "Topola, Serbia || Topola, Srbija || Топола, Сербия || Topola, Serbien || Topola, Serbie",
            biography = "A war veteran and traditional beekeeper from Šumadija. || Solunski borac i pčelar, čuvar šumadijske tradicije. || Ветеран войны и пчеловод из Шумадии. || Ein Kriegsveteran und Imker aus Šumadija. || Un vétéran de guerre et apiculteur de Šumadija."
        ))
        val m1 = dao.insertMember(FamilyMember(
            firstName = "Jelena || Jelena || Елена || Jelena || Jelena",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "FEMALE", birthDate = "30.01.1918",
            birthPlace = "Kragujevac, Serbia || Kragujevac, Srbija || Крагуевац, Сербия || Kragujevac, Serbien || Kragujevac, Serbie",
            biography = "A revered village teacher who preserved folk songs. || Seoska učiteljica koja je čuvala narodne običaje i pesme. || Уважаемая деревенская учительница, сохранившая народные песни. || Eine verehrte Dorfschullehrerin. || Une enseignante de village respectée.",
            spouseId = f1
        ))
        dao.updateMember(dao.getMemberById(f1)!!.copy(spouseId = m1))

        val f2 = dao.insertMember(FamilyMember(
            firstName = "Milan || Milan || Милан || Milan || Milan",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "MALE", birthDate = "01.07.1942",
            birthPlace = "Topola, Serbia || Topola, Srbija || Топола, Сербия || Topola, Serbien || Topola, Serbie",
            biography = "An engineer who built bridges across Yugoslavia. || Građevinski inženjer koji je projektovao mostove širom Jugoslavije. || Инженер, строивший мосты по всей Югославии. || Ein Ingenieur, der Brücken gebaut hat. || Un ingénieur qui a bâti des ponts.",
            fatherId = f1, motherId = m1
        ))
        val m2 = dao.insertMember(FamilyMember(
            firstName = "Milica || Milica || Милица || Milica || Milica",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "FEMALE", birthDate = "14.10.1945",
            birthPlace = "Valjevo, Serbia || Valjevo, Srbija || Валеvo, Сербия || Valjevo, Serbien || Valjevo, Serbie",
            biography = "Chemistry teacher and legendary pastry master. || Profesorka hemije i čuvena autorka domaćih štrudli. || Учитель химии и мастер кулинарии. || Eine Chemielehrerin und Bäckerin. || Enseignante en chimie et pâtissière.",
            spouseId = f2
        ))
        dao.updateMember(dao.getMemberById(f2)!!.copy(spouseId = m2))

        val dad = dao.insertMember(FamilyMember(
            firstName = "Dragan || Dragan || Đragan || Dragan || Dragan",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "MALE", birthDate = "25.04.1970",
            birthPlace = "Topola, Serbia || Topola, Srbija || Топола, Сербия || Topola, Serbien || Topola, Serbie",
            biography = "A Belgrade literature professor collecting antique logs. || Profesor književnosti iz Beograda i sakupljač antikviteta. || Профессор литературы в Белграде. || Ein Literaturprofessor aus Belgrad. || Un professeur de littérature à Belgrade.",
            fatherId = f2, motherId = m2
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Snežana || Snežana || السنيجана || Snežana || Snežana",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "FEMALE", birthDate = "08.11.1973",
            birthPlace = "Belgrade, Serbia || Beograd, Srbija || Белград, Сербия || Belgrad, Serbien || Belgrade, Serbie",
            biography = "A dedicated pediatrician and amateur gardener. || Pedijatar i zaljubljenik u uzgajanje ruža. || Преданный педиатр и садовод-любитель. || Eine engagierte Kinderärztin und Gärtnerin. || Une pédiatre dévouée et jardinière.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        val focus = dao.insertMember(FamilyMember(
            firstName = "Slobodan || Slobodan || Слободан || Slobodan || Slobodan",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "MALE", birthDate = "02.05.1998",
            birthPlace = "Belgrade, Serbia || Beograd, Srbija || Белград, Сербия || Belgrad, Serbien || Belgrade, Serbie",
            biography = "A software engineer who created this app to preserve roots. || Softverski inženjer koji je pokrenuo ovu aplikaciju za istoriju. || Инженер-программист, создавший это приложение. || Softwareentwickler, der diese App erstellt hat. || Ingénieur logiciel qui a créé cette appli.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Katarina || Katarina || Катарина || Katarina || Katarina",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "FEMALE", birthDate = "12.06.1999",
            birthPlace = "Nis, Serbia || Niš, Srbija || Ниш, Сербия || Nis, Serbien || Nis, Serbie",
            biography = "A passionate graphic designer who loves family archives. || Grafički dizajner posvećen očuvanju porodičnih albuma. || Графический дизайнер, увлекающийся архивами. || Grafikdesignerin, die Familienarchive liebt. || Designer graphique aimant les archives familiales.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        dao.insertMember(FamilyMember(
            firstName = "Vuk || Vuk || Вук || Vuk || Vuk",
            lastName = "Marković || Marković || Маркович || Marković || Marković",
            gender = "MALE", birthDate = "14.11.2023",
            birthPlace = "Belgrade, Serbia || Beograd, Srbija || Белград, Сербия || Belgrad, Serbien || Belgrade, Serbie",
            biography = "The youngest descendant who loves picture books. || Najmlađi potomak koji obožava slikovnice. || Самый младший потомок, любит книжки с картинками. || Der jüngste Nachkomme. || Le plus jeune descendant.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedNemanjici() {
        // Stefan Nemanja & Ana
        val gf = dao.insertMember(FamilyMember(
            firstName = "Stefan Nemanja || Stefan Nemanja || Стефан Неманя || Stefan Nemanja || Stefan Nemanja",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "MALE", birthDate = "1113", birthPlace = "Ribnica, Zeta || Ribnica, Zeta || Рибница, Зета || Ribnica, Zeta || Ribnica, Zeta",
            biography = "Grand Župan of Serbia, founder of the dynasty and Saint Simeon. || Veliki župan Raške, osnivač dinastije i Sveti Simeon Mirotočivi. || Великий жупан Сербии, основатель династии. || Großžupan von Serbien, Gründer der Dynastie. || Grand Župan de Serbie, fondateur de la dynastie."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Anastasia (Ana) || Ana (Sveta Anastasija) || Святая Анастасия (Ана) || Anna (St. Anastasia) || Sainte Anastasie (Ana)",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "FEMALE", birthDate = "1125", birthPlace = "Kotor || Kotor || Котор || Kotor || Kotor",
            biography = "Grand Princess consort of Serbia, canonized as Saint Anastasia. || Velika kneginja, supruga Stefana Nemanje i svetica. || Великая княгиня Сербии, канонизированная как Анастасия. || Großfürstin von Serbien. || Grande princesse de Serbie, canonisée.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        // Sons: Stefan Prvovencani & Rastko (Sveti Sava)
        val dad = dao.insertMember(FamilyMember(
            firstName = "Stefan Prvovenčani || Stefan Prvovenčani || Стефан Первовенчанный || Stefan der Erstgekrönte || Stefan le Premier-Couronné",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "MALE", birthDate = "1165", birthPlace = "Stari Ras || Stari Ras || Стари Рас || Stari Ras || Stari Ras",
            biography = "First crowned King of Serbia in 1217, brilliant diplomat and writer. || Prvovenčani kralj Srbije, diplomatski i književni vizionar. || Первый коронованный король Сербии (1217). || Der erste gekrönte König von Serbien (1217). || Premier roi couronné de Serbie (1217).",
            fatherId = gf, motherId = gm
        ))
        dao.insertMember(FamilyMember(
            firstName = "Saint Sava (Rastko) || Sveti Sava (Rastko) || Святитель Савва (Растко) || Heiliger Sava (Rastko) || Saint Sava (Rastko)",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "MALE", birthDate = "1174", birthPlace = "Sari Ras || Stari Ras || Стари Рас || Stari Ras || Stari Ras",
            biography = "First Archbishop of the autocephalous Serbian Orthodox Church. || Prvi arhiepiskop autokefalne Srpske pravoslavne crkve i prosvetitelj. || Первый архиепископ Сербской автокефальной церкви. || Erster Erzbischof der serbischen Kirche. || Premier archevêque de l'Église serbienne autocéphale.",
            fatherId = gf, motherId = gm
        ))

        val mom = dao.insertMember(FamilyMember(
            firstName = "Anna Dandolo || Ana Dandolo || Анна Дандоло || Anna Dandolo || Anna Dandolo",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "FEMALE", birthDate = "1195", birthPlace = "Venice, Italy || Venecija, Italija || Венеция, Италия || Venedig, Italien || Venise, Italie",
            biography = "Serbian Queen Consort, granddaughter of Venetian Doge Enrico Dandolo. || Srpska kraljica, unuka moćnog mletačkog dužda Enrika Dandola. || Сербская королева, внучка венецианского дожа. || Serbische Königin, Enkelin des venezianischen Dogen. || Reine de Serbie, petite-fille du doge vénitien.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        // Son of Prvovencani: Stefan Uros I
        val focus = dao.insertMember(FamilyMember(
            firstName = "Stefan Uroš I || Stefan Uroš I || Стефан Урош I || Stefan Uroš I || Stefan Uroš I",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "MALE", birthDate = "1223", birthPlace = "Stari Ras || Stari Ras || Стари Рас || Stari Ras || Stari Ras",
            biography = "King of Serbia, boosted mining, economy and silver trades. || Kralj Srbije, podstakao razvoj rudarstva, sasa i ekonomije. || Король Сербии, развил горное дело, экономику и торговлю. || König von Serbien, förderte den Bergbau und Wirtschaft. || Roi de Serbie, a stimulé l'exploitation minière et l'économie.",
            fatherId = dad, motherId = mom
        ))

        // Spouse of Uros I: Jelena Anzujska
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Helen of Anjou || Jelena Anžujska || Елена Анжуйская || Helene von Anjou || Hélène d'Anjou",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "FEMALE", birthDate = "1236", birthPlace = "Anjou, France || Anžu, Francuska || Анжу, Франция || Anjou, Frankreich || Anjou, France",
            biography = "Noble Queen Consort, built schools, libraries and Monasteries (Gradac). || Plemenita srpska kraljica, podigla prvu škołu za devojke i Gradac. || Благородная королева, основала первую школу для девочек. || Edle Königin, gründete die erste Mädchenschule. || Noble reine consort, a fondé la première école de filles.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Sons of Uros I: Milutin & Dragutin
        dao.insertMember(FamilyMember(
            firstName = "Stefan Milutin || Stefan Milutin || Стефан Милутин || Stefan Milutin || Stefan Milutin",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "MALE", birthDate = "1253", birthPlace = "Stari Ras || Stari Ras || Стари Рас || Stari Ras || Stari Ras",
            biography = "One of Serbia's most powerful rulers, built over 40 churches. || Jedan od najmoćnijih srpskih vladara, podigao 40 crkava i manastira. || Один из могущественных королей, построил 40 храмов. || Mächtiger König von Serbien, baute 40 Kirchen. || Roi puissant de Serbie, a construit 40 églises.",
            fatherId = focus, motherId = spouse
        ))
        dao.insertMember(FamilyMember(
            firstName = "Stefan Dragutin || Stefan Dragutin || Стефан Драгутин || Stefan Dragutin || Stefan Dragutin",
            lastName = "Nemanjić || Nemanjić || Неманич || Nemanjić || Nemanjić",
            gender = "MALE", birthDate = "1251", birthPlace = "Stari Ras || Stari Ras || Стари Рас || Stari Ras || Stari Ras",
            biography = "King of Serbia and Srem, abdicated throne to brother Milutin. || Kralj Srbije i Srema, predao presto bratu Milutinu. || Король Сербии и Срема, передал трон брату Милутину. || König von Serbien und Srem. || Roi de Serbie et de Syrmie.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedTudors() {
        // Henry VII & Elizabeth of York
        val gf = dao.insertMember(FamilyMember(
            firstName = "Henry VII || Henri VII || Генрих VII || Heinrich VII || Henri VII",
            lastName = "Tudor || Tudor || Тюдор || Tudor || Tudor",
            gender = "MALE", birthDate = "28.01.1457", birthPlace = "Pembroke, Wales",
            biography = "First monarch of the House of Tudor, won the War of the Roses. || Prvi vladar dinastije Tjudor, pobednik rata dveju ruža. || Первый монарх Тюдор, завершил войну Алой и Белой розы. || Gründer der Tudor-Dynastie, gewann die Rosenkriege. || Premier monarque Tudor, vainqueur de la guerre des Deux-Roses."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Elizabeth || Elizabeta || Елизавета Йоркская || Elisabeth von York || Élisabeth d'York",
            lastName = "of York || od Jorka || Йорк || von York || d'York",
            gender = "FEMALE", birthDate = "11.02.1466", birthPlace = "London, England",
            biography = "Queen Consort of England, united the House of Lancaster and York. || Engleska kraljica, ujedinila crvenu i belu ružu Jorka i Lankastera. || Королева-консорт Англии, объединила дома Ланкастеров и Йорков. || Königin von England, einte die Häuser Lancaster und York. || Reine consort d'Angleterre, unit les maisons d'York et de Lancastre.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        // Son: Henry VIII
        val dad = dao.insertMember(FamilyMember(
            firstName = "Henry VIII || Henri VIII || Генрих VIII || Heinrich VIII || Henri VIII",
            lastName = "Tudor || Tudor || Тюдор || Tudor || Tudor",
            gender = "MALE", birthDate = "28.06.1491", birthPlace = "Greenwich, London",
            biography = "Famous for his six marriages and establishing the Church of England. || Poznat po šest brakova i uspostavljanju Crkve Engleske. || Известен шестью браками и созданием Англиканской церкви. || Berühmt für sechs Ehen und die Gründung der Church of England. || Célèbre pour ses six mariages et la fondation de l'Église d'Angleterre.",
            fatherId = gf, motherId = gm
        ))
        // Jane Seymour (mother of Edward VI)
        val mom = dao.insertMember(FamilyMember(
            firstName = "Jane || Džejn || Джейн Сеймур || Jane || Jeanne",
            lastName = "Seymour || Sejmur || Сеймур || Seymour || Seymour",
            gender = "FEMALE", birthDate = "1508", birthPlace = "Wiltshire, England",
            biography = "Henry VIII's third wife who gave him his desired male heir. || Treća supruga Henrija VIII koja mu je podarila muškog naslednika. || Третья жена Генриха VIII, родившая ему желанного наследника. || Dritte Frau Heinrichs VIII., gebar den männlichen Thronfolger. || Troisième épouse d'Henri VIII, qui lui donna l'héritier mâle tant désiré.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        // Focus: Edward VI
        val focus = dao.insertMember(FamilyMember(
            firstName = "Edward VI || Edvard VI || Эдуард VI || Eduard VI || Édouard VI",
            lastName = "Tudor || Tudor || Тюдор || Tudor || Tudor",
            gender = "MALE", birthDate = "12.10.1537", birthPlace = "Hampton Court, London",
            biography = "Crowned king at age 9, England's first Protestant monarch. || Krunisan u devetoj godini, prvi protestantski kralj dinastije. || Коронован в 9 лет, первый монарх-протестант Англии. || Gekrönt im Alter von 9 Jahren, erster protestantischer König. || Couronné de 9 ans, premier monarque protestant d'Angleterre.",
            fatherId = dad, motherId = mom
        ))

        // Siblings: Elizabeth I & Mary I
        dao.insertMember(FamilyMember(
            firstName = "Elizabeth I || Elizabeta I || Елизавета I || Elisabeth I || Élisabeth I",
            lastName = "Tudor || Tudor || Тюдор || Tudor || Tudor",
            gender = "FEMALE", birthDate = "07.09.1533", birthPlace = "Greenwich, London",
            biography = "The 'Virgin Queen', ruled during England's golden Elizabethan Era. || 'Devičanska kraljica', vladala tokom zlatnog Elizaketanskog doba. || 'Королева-дественница', правила в золотой век Англии. || Die 'Jungfräuliche Königin', regierte im Goldenen Zeitalter. || La 'Reine Vierge', régna durant l'âge d'or élisabéthain.",
            fatherId = dad
        ))
        dao.insertMember(FamilyMember(
            firstName = "Mary I || Marija I (Krvava) || Мария I (Кровавая) || Maria I (Blutige) || Marie I (Bloody Mary)",
            lastName = "Tudor || Tudor || Тюдор || Tudor || Tudor",
            gender = "FEMALE", birthDate = "18.02.1516", birthPlace = "Greenwich, London",
            biography = "First undisputed Queen Regnant of England, nicknamed Bloody Mary. || Prva nesporna kraljica vladarka, poznata kao 'Krvava Meri'. || Первая бесспорная королева Англии, 'Кровавая Мэри'. || Erste anerkannte herrschende Königin, bekannt als 'Bloody Mary'. || Première reine régnante incontestée d'Angleterre, 'la Sanglante'.",
            fatherId = dad
        ))
    }

    private suspend fun seedRomanovs() {
        // Alexander II & Maria Alexandrovna
        val gf = dao.insertMember(FamilyMember(
            firstName = "Alexander II || Aleksandar II || Александр II || Alexander II || Alexandre II",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "MALE", birthDate = "29.04.1818", birthPlace = "Moscow, Russia",
            biography = "Emperor of Russia who liberated the serfs in 1861. || Car Rusije koji je ukinuo kmetstvo i sproveo velike reforme. || Александр Освободитель, отменил крепостное право в 1861 году. || Zar von Russland, der 1861 die Leibeigenschaft aufhob. || Empereur de Russie qui libéra les serfs en 1861."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Maria Alexandrovna || Marija Aleksandrovna || Мария Александровна || Maria Alexandrowna || Marie Alexandrovna",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "FEMALE", birthDate = "08.08.1824", birthPlace = "Darmstadt, Germany",
            biography = "Empress Consort of Russia, founder of the Russian Red Cross. || Carica Rusije, pokretač i osnivač ruskog Crvenog krsta. || Императрица-консорт, основала Российский Красный Железный Крест. || Kaiserin von Russland, Gründerin des Roten Kreuzes. || Impératrice de Russie, fondatrice de la Croix-Rouge russe.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        // Son: Alexander III
        val dad = dao.insertMember(FamilyMember(
            firstName = "Alexander III || Aleksandar III || Александр III || Alexander III || Alexandre III",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "MALE", birthDate = "10.03.1845", birthPlace = "St. Petersburg, Russia",
            biography = "The Peacemaker, highly conservative Emperor who preserved stability. || Car 'Mirotvorac', izuzetno konzervativan vladar velikog rasta. || Александр Миротворец, укрепил государственность. || Der Friedensstifter, hochkonservativer Herrscher. || Le Pacificateur, empereur très conservateur.",
            fatherId = gf, motherId = gm
        ))
        // Empress Maria Feodorovna (Dagmar of Denmark)
        val mom = dao.insertMember(FamilyMember(
            firstName = "Maria Feodorovna || Marija Fjodorovna || Мария Фёдоровна || Maria Fjodorowna || Marie Feodorovna",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "FEMALE", birthDate = "26.11.1847", birthPlace = "Copenhagen, Denmark",
            biography = "Danish Princess Dagmar, mother of the last tsar Nicholas II. || Danska princeza Dagmar, majka poslednjeg ruskog cara Nikolaja II. || Датская принцесса Дагмар, мать последнего царя Николая II. || Dänische Prinzessin Dagmar, Mutter des letzten Zaren. || Princesse Dagmar de Danemark, mère du dernier tsar.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        // Focus: Nicholas II
        val focus = dao.insertMember(FamilyMember(
            firstName = "Nicholas II || Nikolaj II || Николай II || Nikolaus II || Nicolas II",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "MALE", birthDate = "18.05.1868", birthPlace = "Tsarskoye Selo, Russia",
            biography = "Last Emperor of Russia, canonized as Saint Nicholas the Passion-Bearer. || Poslednji ruski imperator, kanonizovan u Ruskoj pravoslavnoj crkvi. || Последний Император России, канонизирован Собором. || Der letzte russische Kaiser, heiliggesprochen. || Le dernier empereur de Russie, canonisé.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Alexandra Feodorovna || Aleksandra Fjodorovna || Александра Фёдоровна || Alexandra Fjodorowna || Alexandra Feodorovna",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "FEMALE", birthDate = "06.06.1872", birthPlace = "Darmstadt, Germany",
            biography = "Empress consort, granddaughter of Britain's Queen Victoria. || Carica supruga, tragična vladarka i unuka kraljice Viktorije. || Императрица, канонизированная великая мученица. || Kaiserin Alix von Hessen, Enkelin der Königin Victoria. || Impératrice de Russie, petite-fille de la reine Victoria.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Children: Anastasia & Alexei
        dao.insertMember(FamilyMember(
            firstName = "Anastasia || Anastasija || Анастасия || Anastasia || Anastasia",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "FEMALE", birthDate = "18.06.1901", birthPlace = "Peterhof, Russia",
            biography = "Grand Duchess, whose rumored survival sparked global blockbusters. || Velika kneginja, o čijem su navodnom preživljavanju kružile legende. || Великая княжна Анастасия, с которой связано много легенд. || Großfürstin Anastasia, deren vermeintliches Überleben Legenden schuf. || Grande-duchesse Anastasia, dont la rumeur de survie créa des légendes.",
            fatherId = focus, motherId = spouse
        ))
        dao.insertMember(FamilyMember(
            firstName = "Alexei || Aleksej || Алексей || Alexei || Alexei",
            lastName = "Romanov || Romanov || Романов || Romanow || Romanov",
            gender = "MALE", birthDate = "12.08.1904", birthPlace = "Peterhof, Russia",
            biography = "Tsarevich and rightful heir to the throne, suffered from hemophilia. || Carević Aleksej, jedini naslednik trona koji je bolovao od hemofilije. || Цесаревич Алексей, страдал гемофилией. || Zarewitsch Alexei, litt an Hämophilie. || Tsarévitch Alexis, souffrait d'hémophilie.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedHabsburgs() {
        val gf = dao.insertMember(FamilyMember(
            firstName = "Francis II || Franc II || Франц II || Franz II || François II",
            lastName = "Habsburg || Habsburg || Габсбург || Habsburg || Habsbourg",
            gender = "MALE", birthDate = "12.02.1768", birthPlace = "Florence, Italy",
            biography = "The last Holy Roman Emperor and first Emperor of Austria. || Poslednji car Svetog rimskog carstva i prvi austrijski car. || Последний император Священной Римской империи. || Der letzte Kaiser des Heiligen Römischen Reiches. || Dernier empereur du Saint-Empire."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Maria Theresa || Marija Terezija || Мария Терезия || Maria Theresia || Marie-Thérèse",
            lastName = "of Naples || od Napulja || Неаполитанская || von Neapel || de Naples",
            gender = "FEMALE", birthDate = "06.06.1772", birthPlace = "Naples, Italy",
            biography = "Empress Consort of the Holy Roman Empire and Austria. || Austrijska carica, supruga Franca II i velika pokroviteljka. || Императрица-консорт, супруга Франца II. || Letzte römisch-deutsche Kaiserin. || Impératrice consort d'Autriche.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        val dad = dao.insertMember(FamilyMember(
            firstName = "Franz Karl || Franc Karlo || Франц Карл || Franz Karl || François-Charles",
            lastName = "Habsburg || Habsburg || Габсбург || Habsburg || Habsbourg",
            gender = "MALE", birthDate = "17.12.1802", birthPlace = "Vienna, Austria",
            biography = "Archduke of Austria, father of Emperor Franz Joseph. || Austrijski nadvojvoda, otac slavnog cara Franca Jozefa. || Эрцгерцог Австрийский, отец Франца Иосифа. || Erzherzog von Österreich, Vater von Kaiser Franz Joseph. || Archiduc d'Autriche, père de François-Joseph.",
            fatherId = gf, motherId = gm
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Sophie of Bavaria || Sofija od Bavarske || София Баварская || Sophie von Bayern || Sophie de Bavière",
            lastName = "Habsburg || Habsburg || Габсбург || Habsburg || Habsbourg",
            gender = "FEMALE", birthDate = "27.01.1805", birthPlace = "Munich, Germany",
            biography = "Powerful Archduchess behind the Austrian imperial court throne. || Moćna nadvojvotkinja i 'jedini muškarac u palati' u Beču. || Эрцгерцогиня София, влиятельная фигура при дворе. || Einflussreiche Erzherzogin am Wiener Hof. || Archiduchesse influente à la cour de Vienne.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        val focus = dao.insertMember(FamilyMember(
            firstName = "Franz Joseph I || Franc Jozef I || Франц Иосиф I || Franz Joseph I || François-Joseph I",
            lastName = "Habsburg || Habsburg || Габсбург || Habsburg || Habsbourg",
            gender = "MALE", birthDate = "18.08.1830", birthPlace = "Schönbrunn, Vienna",
            biography = "Emperor of Austria-Hungary, ruled for an impressive 68 years. || Car Austrogarske, jedan od najdugovečnijih evropskih vladara (68 godina). || Император Австро-Венгрии, правил 68 лет. || Kaiser von Österreich-Ungarn, regierte 68 Jahre lang. || Empereur d'Autriche-Hongrie, régna 68 ans.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Elisabeth (Sisi) || Elizabeta (Sisi) || Елизавета (Сиси) || Elisabeth (Sisi) || Élisabeth (Sissi)",
            lastName = "Habsburg || Habsburg || Габсбург || Habsburg || Habsbourg",
            gender = "FEMALE", birthDate = "24.12.1837", birthPlace = "Munich, Germany",
            biography = "Empress of Austria, famous for her outstanding beauty and free spirit. || Legendarna carica Sisi, čuvena po slobodnom duhu i lepoti. || Императрица Сиси, известная своей красотой и свободолюбием. || Kaiserin Sisi, berühmt für Schönheit und Freiheitsliebe. || Impératrice Sissi, célèbre pour sa beauté et son esprit libre.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Child: Rudolf
        dao.insertMember(FamilyMember(
            firstName = "Rudolf || Rudolf || Рудольф || Rudolf || Rodolphe",
            lastName = "Habsburg || Habsburg || Габсбург || Habsburg || Habsbourg",
            gender = "MALE", birthDate = "21.08.1858", birthPlace = "Laxenburg, Austria",
            biography = "Crown Prince of Austria whose tragic death shook the empire. || Prestolonaslednik čija je misteriozna smrt u Majerlingu potresla svet. || Кронпринц Австрии, чья трагическая смерть потрясла мир. || Kronprinz von Österreich, tragisch im Schloss Mayerling verstorben. || Prince héritier d'Autriche, mort tragiquement à Mayerling.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedBourbons() {
        val gf = dao.insertMember(FamilyMember(
            firstName = "Henry IV || Henri IV || Генрих IV || Heinrich IV || Henri IV",
            lastName = "Bourbon || Bourbon || Бурбон || Bourbon || Bourbon",
            gender = "MALE", birthDate = "13.12.1553", birthPlace = "Pau, France",
            biography = "First French monarch of the House of Bourbon, promulgated the Edict of Nantes. || Prvi burbonski kralj Francuske, autor Nantskog edikta vernosti. || Первый король Франции из Бурбонов, издал Нантский эдикт. || Erster König aus dem Haus Bourbon, erließ das Edikt von Nantes. || Premier roi Bourbon de France, promulgua l'Édit de Nantes."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Marie de' Medici || Marija de Mediči || Мария Медичи || Maria von Medici || Marie de Médicis",
            lastName = "Bourbon || Bourbon || Бурбон || Bourbon || Bourbon",
            gender = "FEMALE", birthDate = "26.04.1575", birthPlace = "Florence, Italy",
            biography = "Queen Consort of France, great arts patron of Peter Paul Rubens. || Francuska kraljica, velika zaštitnica umetnosti i mecena Rubensa. || Королева Франции, покровительница искусств и Рубенса. || Königin von Frankreich, große Förderin von Rubens. || Reine consort de France, grande protectrice des arts.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        val dad = dao.insertMember(FamilyMember(
            firstName = "Louis XIII || Luj XIII || Людовик XIII || Ludwig XIII || Louis XIII",
            lastName = "Bourbon || Bourbon || Бурбон || Bourbon || Bourbon",
            gender = "MALE", birthDate = "27.09.1601", birthPlace = "Fontainebleau, France",
            biography = "King of France, ruled alongside the famous Cardinal Richelieu. || Kralj Francuske koji je vladao uz slavnog kardinala Rišeljea. || Король Франции, правил вместе с кардиналом Ришелье. || König von Frankreich, regierte mit Kardinal Richelieu. || Roi de France, régna aux côtés du cardinal de Richelieu.",
            fatherId = gf, motherId = gm
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Anne of Austria || Ana Austrijska || Анна Австрийская || Anna von Österreich || Anne d'Autriche",
            lastName = "Bourbon || Bourbon || Бурбон || Bourbon || Bourbon",
            gender = "FEMALE", birthDate = "22.09.1601", birthPlace = "Valladolid, Spain",
            biography = "Queen consort, regent of France, mother of the Sun King. || Španska princeza i francuska kraljica regent, majka Luja XIV. || Испанская инфанта, королева Франции, мать Короля-Солнца. || Spanische Infantin, Königin von Frankreich, Mutter des Sonnenkönigs. || Infante d'Espagne, reine de France, mère du Roi-Soleil.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        val focus = dao.insertMember(FamilyMember(
            firstName = "Louis XIV || Luj XIV || Людовик XIV || Ludwig XIV || Louis XIV",
            lastName = "Bourbon || Bourbon || Бурбон || Bourbon || Bourbon",
            gender = "MALE", birthDate = "05.09.1638", birthPlace = "Saint-Germain, France",
            biography = "The legendary 'Sun King', built the luxury Palace of Versailles. || Legendarni 'Kralj Sunce', izgradio dvorac Versaj i vladao 72 godine. || Легендарный Король-Солнце, построивший Версаль. || Der Sonnenkönig, erbaute das Schloss Versailles. || L'illustre 'Roi-Soleil', bâtisseur du château de Versailles.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Maria Theresa || Marija Terezija || Мария Терезия || Maria Theresia || Marie-Thérèse d'Autriche",
            lastName = "of Spain || od Španije || Испанская || von Spanien || d'Espagne",
            gender = "FEMALE", birthDate = "10.09.1638", birthPlace = "Madrid, Spain",
            biography = "Queen Consort, first wife of the Sun King, aunt of Charles II. || Španska princeza, prva supruga Luja XIV. || Королева Франции, первая супруга Людовика XIV. || Königin von Frankreich, erste Ehefrau des Sonnenkönigs. || Reine consort de France, première époque du Roi-Soleil.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        dao.insertMember(FamilyMember(
            firstName = "Louis || Luj (Veliki Dofen) || Людовик (Великий Дофин) || Ludwig (Grand Dauphin) || Louis (Le Grand Dauphin)",
            lastName = "Bourbon || Bourbon || Бурбон || Bourbon || Bourbon",
            gender = "MALE", birthDate = "01.11.1661", birthPlace = "Fontainebleau, France",
            biography = "The Grand Dauphin, legitimate eldest son and heir of Louis XIV. || Veliki dofen, najstariji i legitimni naslednik trona Luja XIV. || Великий Дофин, старший сын и наследник Людовика XIV. || Der Grand Dauphin, ältester Sohn des Sonnenkönigs. || Le Grand Dauphin, fils aîné et héritier de Louis XIV.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedOttomans() {
        // Bayezid II & Gulbahar
        val gf = dao.insertMember(FamilyMember(
            firstName = "Bayezid II || Bajazit II || Баязид II || Bayezid II || Bayezid II",
            lastName = "Osman || Osman || Осман || Osman || Osman",
            gender = "MALE", birthDate = "03.12.1447", birthPlace = "Didymoteicho, Thrace",
            biography = "Ottoman Sultan who consolidated the empire and welcomed Jewish refugees. || Otomanski sultan koji je primio jevrejske izbeglice iz Španije. || Султан, закрепивший завоевания отца Мехмеда Завоевателя. || Osmanischer Sultan, festigte das Reich. || Sultan ottoman, consolida l'empire."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Gülbahar Hatun || Gulbahar Hatun || Гюльбахар Хатун || Gülbahar Hatun || Gülbahar Hatun",
            lastName = "Osman || Osman || Осман || Osman || Osman",
            gender = "FEMALE", birthDate = "1453", birthPlace = "Trabzon",
            biography = "Valide Hatun and mother of Sultan Selim I. || Majka sultana Selima I i moćna carica. || Мать султана Селима I, влиятельная фигура. || Mutter von Sultan Selim I. || Mère du sultan Selim I.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        // Selim I & Hafsa Sultan
        val dad = dao.insertMember(FamilyMember(
            firstName = "Selim I || Selim I || Селим I || Selim I || Selim I",
            lastName = "Osman || Osman || Осман || Osman || Osman",
            gender = "MALE", birthDate = "10.10.1470", birthPlace = "Amasya",
            biography = "Selim the Grim, doubled the empire's size and conquered Egypt. || Selim 'Yavuz' (Grozni), osvojio Egipat i proširio carstvo. || Селим Грозный, завоевал Египет и халифат. || Selim der Gestrenge, eroberte Ägypten. || Selim Ier le Terrible, doubla la taille de l'empire.",
            fatherId = gf, motherId = gm
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Hafsa Sultan || Hafsa Sultan || Хафса Султан || Hafsa Sultan || Hafsa Sultan",
            lastName = "Osman || Osman || Осман || Osman || Osman",
            gender = "FEMALE", birthDate = "1479", birthPlace = "Crimea",
            biography = "First official Valide Sultan of the Ottoman Empire, mother of Suleiman. || Prva zvanična Valide sultanija i majka Sulejmana Veličanstvenog. || Первая Валиде-султан Османской империи, мать Сулеймана. || Erste offizielle Valide Sultan, Mutter von Süleyman. || Première Valide Sultan de l'Empire ottoman, mère de Soliman.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        // Focus: Suleiman I
        val focus = dao.insertMember(FamilyMember(
            firstName = "Suleiman I || Sulejman I || Сулейман I || Süleyman I || Soliman I",
            lastName = "Osman || Osman || Осман || Osman || Osman",
            gender = "MALE", birthDate = "06.11.1494", birthPlace = "Trabzon",
            biography = "Suleiman the Magnificent / Lawgiver, peak of the Empire. || Sulejman Veličanstveni (Zakonodavac), najdugovečniji i najslavniji sultan. || Сулейман Великолепный, законодатель, эпоха расцвета. || Süleyman der Prächtige, größte Ausdehnung des Reiches. || Soliman le Magnifique, apogée de l'Empire ottoman.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Hürrem Sultan || Hurem Sultan || Хюррем Султан || Hürrem Sultan || Roxelane (Hürrem)",
            lastName = "Osman || Osman || Осман || Osman || Osman",
            gender = "FEMALE", birthDate = "1502", birthPlace = "Ruthenia (Ukraine)",
            biography = "Roxelana, slave girl who became Empress and started the Sultanate of Women. || Slavna Hurem (Rokselana), bivša robinja koja je započela vladavinu žena. || Легендарная Роксолана, султанша, начавшая женский султанат. || Legendäre Hürrem Sultan, einflussreichste Ehefrau Süleymans. || Roxelane, esclave devenue impératrice régente.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Son: Selim II
        dao.insertMember(FamilyMember(
            firstName = "Selim II || Selim II || Селим II || Selim II || Selim II",
            lastName = "Osman || Osman || Осман || Osman || Osman",
            gender = "MALE", birthDate = "28.05.1524", birthPlace = "Constantinople",
            biography = "Selim the Blond, son of Suleiman who ruled during Pax Ottomanica. || Selim II (Plavi), nasledio presto nakon očeve briljantne ere. || Селим II (Блондин), сын Сулеймана и Роксоланы. || Selim II., Sohn von Süleyman. || Selim II, souverain de la Pax Ottomanica.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedMedici() {
        val gf = dao.insertMember(FamilyMember(
            firstName = "Cosimo de' Medici || Kozimo de Mediči || Козимо Медичи || Cosimo de' Medici || Cosme de Médicis",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "MALE", birthDate = "27.09.1389", birthPlace = "Florence, Italy",
            biography = "Cosimo the Elder, Pater Patriae, founder of the Medici banking empire. || Kozimo Stariji, osnivač bogatstva porodice Mediči i vladar Firence. || Козимо Старый, основатель банковской империи Медичи. || Cosimo der Ältere, Begründer der Medici-Macht. || Cosme l'Ancien, fondateur de la dynastie financière de Florence."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Contessina de' Bardi || Kontesina de Bardi || Контессина де Барди || Contessina de' Bardi || Contessina de' Bardi",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "FEMALE", birthDate = "1390", birthPlace = "Florence, Italy",
            biography = "Noblewoman from the ancient banking Bardi family, wife of Cosimo. || Supruga Kozima, poreklom iz slavne italijanske bankarske loze Bardi. || Супруга Козимо из древнего банковского рода Барди. || Ehefrau von Cosimo aus dem mächtigen Hause Bardi. || Épouse de Cosme de Médicis, issue de l'illustre maison de Bardi.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        val dad = dao.insertMember(FamilyMember(
            firstName = "Piero de' Medici || Pjero de Mediči || Пьеро Медичи || Piero de' Medici || Pierre de Médicis",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "MALE", birthDate = "19.09.1416", birthPlace = "Florence, Italy",
            biography = "Piero the Gouty, de facto ruler of Florence from 1464 to 1469. || Pjero Naduti, vladao tokom teških političkih zavera u Firenci. || Пьеро Подагрик, правитель Флоренции. || Piero der Gichtige, de facto Herrscher von Florenz. || Pierre le Goutteux, dirigeant de Florence.",
            fatherId = gf, motherId = gm
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Lucrezia Tornabuoni || Lukrecija Tornabuoni || Лукреция Торнабуони || Lucrezia Tornabuoni || Lucrèce Tornabuoni",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "FEMALE", birthDate = "22.06.1427", birthPlace = "Florence, Italy",
            biography = "An influential writer and political advisor to her magnificent son. || Pesnikinja i savetnica svog sina Lorenca Veličanstvenog. || Писательница и советница при флорентийском дворе. || Dichterin und Ratgeberin ihres Sohnes Lorenzo. || Poétesse et conseillère politique de son fils Laurent.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        val focus = dao.insertMember(FamilyMember(
            firstName = "Lorenzo de' Medici || Lorenco de Mediči || Лоренцо Медичи || Lorenzo de' Medici || Laurent de Médicis",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "MALE", birthDate = "01.01.1449", birthPlace = "Florence, Italy",
            biography = "Lorenzo the Magnificent, patron of Michelangelo, Da Vinci, and Botticelli. || Lorenco Veličanstveni, mecena Mikelanđela, Da Vinčija i Botičelija. || Лоренцо Великолепный, величайший покровитель художников Возрождения. || Lorenzo der Prächtige, Förderer von Michelangelo. || Laurent le Magnifique, mécène de Michel-Ange.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Clarice Orsini || Klaris Orsini || Клариче Орсини || Clarice Orsini || Clarice Orsini",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "FEMALE", birthDate = "1453", birthPlace = "Rome, Italy",
            biography = "Roman noblewoman who brought an aristocratic alliance to Florence. || Rimska plemkinja, unela plemićki vojni savez u elitu Firence. || Римская аристократка, супруга Лоренцо. || Römische Adlige, Ehefrau von Lorenzo. || Aristocrate romaine, alliance prestigieuse pour Florence.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Children: Piero & Pope Leo X
        dao.insertMember(FamilyMember(
            firstName = "Piero the Unfortunate || Pjero Nesrećni || Пьеро Глупый || Piero der Unglückliche || Pierre l'Infortuné",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "MALE", birthDate = "15.02.1472", birthPlace = "Florence, Italy",
            biography = "Briefly ruled Florence until he was exiled due to French invasion. || Vladao kratko, proteran iz Firence zbog Francuza. || Изгнан из Флоренции за уступки Франции. || Kurze unglückliche Regierungszeit. || Chassé de Florence lors de l'invasion française.",
            fatherId = focus, motherId = spouse
        ))
        dao.insertMember(FamilyMember(
            firstName = "Giovanni (Pope Leo X) || Đovani (Papa Lav X) || Джованни (Папа Лев X) || Giovanni (Papst Leo X) || Jean (Pape Léon X)",
            lastName = "Medici || Mediči || Медичи || Medici || Médicis",
            gender = "MALE", birthDate = "11.12.1475", birthPlace = "Florence, Italy",
            biography = "The Pope who excommunicated Martin Luther and funded St. Peter's Basilica. || Rimski papa koji je ekskomunicirao Luterance i završavao crkvu Sv. Petra. || Римский папа, отлучивший Мартина Лютера от церкви. || Papst, der Martin Luther exkommunizierte. || Pape qui excommunia Martin Luther.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedYamato() {
        val gf = dao.insertMember(FamilyMember(
            firstName = "Emperor Meiji || Car Meiđi || Император Мэйдзи || Kaiser Meiji || Empereur Meiji",
            lastName = "Yamato || Yamato || Ямато || Yamato || Yamato",
            gender = "MALE", birthDate = "03.11.1852", birthPlace = "Kyoto, Japan",
            biography = "Mutsuhito, led Japan's rapid modernization and industrialization. || Car Mucuhito, predvodio brzu modernizaciju i restauraciju Japana. || Император Муцухито, открывший Японию миру. || Kaiser Mutsuhito, leitete die Meiji-Restauration ein. || Empereur Mutsuhito, artisan de la modernisation du Japon."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Empress Shōken || Carica Šoken || Императрица Сёкэн || Kaiserin Shōken || Impératrice Shōken",
            lastName = "Yamato || Yamato || Ямато || Yamato || Yamato",
            gender = "FEMALE", birthDate = "11.01.1849", birthPlace = "Kyoto, Japan",
            biography = "First Imperial Consort to adopt western clothes, supported Red Cross. || Prva carica koja je prihvatila zapadni stil odeće, podržala Crveni krst. || Императрица, первая надевшая европейское платье. || Erste Kaiserin, die europäische Mode annahm. || Première impératrice à adopter les vêtements occidentaux.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        val dad = dao.insertMember(FamilyMember(
            firstName = "Emperor Taishō || Car Taišo || Император Тайсё || Kaiser Taishō || Empereur Taishō",
            lastName = "Yamato || Yamato || Ямато || Yamato || Yamato",
            gender = "MALE", birthDate = "31.08.1879", birthPlace = "Tokyo, Japan",
            biography = "Yoshihito, ruled during a period of expanding democratic movements. || Car Jošihito, vladao u vreme širenja demokratskih sloboda u Japanu. || Император Ёсихито, эпоха демократии Тайсё. || Kaiser Yoshihito, regierte in liberaler Ära. || Empereur Yoshihito, régna durant la démocratie de Taishō.",
            fatherId = gf, motherId = gm
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Empress Teimei || Carica Teimei || Императрица Тэймэй || Kaiserin Teimei || Impératrice Teimei",
            lastName = "Yamato || Yamato || Ямато || Yamato || Yamato",
            gender = "FEMALE", birthDate = "25.06.1884", birthPlace = "Tokyo, Japan",
            biography = "Empress consort, famous for promoting silkworm farming. || Carica supruga, pokroviteljka proizvodnje svile i ekologije. || Императрица Садако, покровительница шелководства. || Kaiserin Sadako, Förderin der Seidenraupenzucht. || Impératrice Sadako, protectrice de la sériciculture.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        val focus = dao.insertMember(FamilyMember(
            firstName = "Emperor Shōwa || Car Šova (Hirohito) || Император Сёва (Хирохито) || Kaiser Shōwa (Hirohito) || Empereur Shōwa (Hirohito)",
            lastName = "Yamato || Yamato || Ямато || Yamato || Yamato",
            gender = "MALE", birthDate = "29.04.1901", birthPlace = "Tokyo, Japan",
            biography = "Hirohito, longest-reigning emperor who transitioned Japan to peace. || Car Hirohito, najdugovečniji car koji je vratio Japan u svetski mir. || Император Хирохито, эпоха восстановления страны. || Kaiser Hirohito, der kaiserliche Weg in die Moderne. || Empereur Hirohito, plus long règne impérial, accompagna la reconstruction du pays.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Empress Kōjun || Carica Kodžun || Императрица Кодзюн || Kaiserin Kōjun || Impératrice Kōjun",
            lastName = "Yamato || Yamato || Ямато || Yamato || Yamato",
            gender = "FEMALE", birthDate = "06.03.1903", birthPlace = "Tokyo, Japan",
            biography = "Nagako, active imperial painter, mother of Emperor Akihito. || Carica Nagako, slikarka i majka današnjeg počasnog cara Akihita. || Императрица Нагако, мать императора Акихито. || Kaiserin Nagako, Mutter von Kaiser Akihito. || Impératrice Nagako, peintre accomplie et mère d'Akihito.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Child: Akihito
        dao.insertMember(FamilyMember(
            firstName = "Emperor Akihito || Car Akihito || Император Акихито || Kaiser Akihito || Empereur Akihito",
            lastName = "Yamato || Yamato || Ямато || Yamato || Yamato",
            gender = "MALE", birthDate = "23.12.1933", birthPlace = "Tokyo, Japan",
            biography = "The Heisei Emperor, first in modern times to abdicate to his son. || Počasni car Akihito (Heisei), prvi koji je mirno abdicirao u korist sina. || Император эпохи Хэйсэй, отрёкся в пользу сына в 2019 году. || Heisei-Kaiser, dankte 2019 freiwillig ab. || Empereur Heisei, abdiqua pacifiquement en favor de son fils en 2019.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedJulioClaudian() {
        val gf = dao.insertMember(FamilyMember(
            firstName = "Julius Caesar || Julije Cezar || Юлий Цезарь || Julius Cäsar || Jules César",
            lastName = "Julius || Julius || Юлий || Julius || Julius",
            gender = "MALE", birthDate = "100 BC", birthPlace = "Rome, Italy",
            biography = "Dictator perpetuo of Rome, paved the transition to the Roman Empire. || Diktator Rima, postavio temelje Rimskog Carstva svojim padom. || Бессменный диктатор Рима, подготовил империю. || Diktator auf Lebenszeit, ebnete den Weg zur Kaiserzeit. || Dictateur perpétuel de Rome, prépara la transition impériale."
        ))
        // Note: Augustus was Caesar's adoptive son
        val dad = dao.insertMember(FamilyMember(
            firstName = "Augustus Caesar || Avgust Oktavijan || Октавиан Август || Augustus || Auguste",
            lastName = "Julio-Claudian || Julio-Claudian || Юлиев-Клавдиев || Julio-Claudian || Julio-Claudien",
            gender = "MALE", birthDate = "23.09.63 BC", birthPlace = "Rome, Italy",
            biography = "First Roman Emperor, ushered in the historic Pax Romana. || Prvi rimski car, započeo istorijsku dvo-vekovnu Pax Romanu. || Первый римский император, начавший эпоху Pax Romana. || Erster römischer Kaiser, begründete die Pax Romana. || Premier empereur romain, instigateur de la Pax Romana.",
            fatherId = gf
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Livia Drusilla || Livija Druzila || Ливия Друзилла || Livia Drusilla || Livie",
            lastName = "Julio-Claudian || Julio-Claudian || Юлиев-Клавдиев || Julio-Claudian || Julio-Claudien",
            gender = "FEMALE", birthDate = "30.01.59 BC", birthPlace = "Rome, Italy",
            biography = "Empress Consort, advisor, mother of Tiberius, deified by Claudius. || Carica, savetnica carstva, majka Tibereja i kćerka rimske slave. || Римская императрица, мать императора Тиберия. || Mächtige Kaiserin, Mutter des Tiberius. || Impératrice de Rome, conseillère politique et mère de Tibère.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        val focus = dao.insertMember(FamilyMember(
            firstName = "Tiberius || Tiberije || Тиберий || Tiberius || Tibère",
            lastName = "Julio-Claudian || Julio-Claudian || Юлиев-Клавдиев || Julio-Claudian || Julio-Claudien",
            gender = "MALE", birthDate = "16.11.42 BC", birthPlace = "Rome, Italy",
            biography = "Second Roman Emperor, one of Rome's greatest military generals. || Drugi rimski car, povučeni i gorki vladar, genijalni vojni vođa. || Второй римский император, великий полководец. || Zweiter römischer Kaiser, hervorragender Feldherr. || Deuxième empereur romain, brillant stratège militaire.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Julia the Elder || Julija Starija || Юлия Старшая || Julia die Ältere || Julia l'Aînée",
            lastName = "Julio-Claudian || Julio-Claudian || Юлиев-Клавдиев || Julio-Claudian || Julio-Claudien",
            gender = "FEMALE", birthDate = "39 BC", birthPlace = "Rome, Italy",
            biography = "Daughter of Augustus, married to Tiberius. || Kćerka cara Avgusta, udata za Tiberija radi dinastičkog ujedinjenja. || Единственная дочь Августа, супруга Тиберия. || Einzige Tochter des Augustus. || Fille unique d'Auguste, mariée à Tibère.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Child: Drusus Julius Caesar
        dao.insertMember(FamilyMember(
            firstName = "Drusus || Drus || Друз Младший || Drusus || Drusus",
            lastName = "Julio-Claudian || Julio-Claudian || Юлиев-Клавдиев || Julio-Claudian || Julio-Claudien",
            gender = "MALE", birthDate = "14 BC", birthPlace = "Rome, Italy",
            biography = "The son of Tiberius, poisoned by the ambitious Sejanus. || Sin cara Tiberija, otrovan zaverom vrhovnog zapovednika Sejana. || Сын Тиберия, коварно отравленный Сеяном. || Sohn des Tiberius, Opfer einer Giftverschwörung. || Fils de Tibère, empoisonné lors de la conspiration de Séjan.",
            fatherId = focus, motherId = spouse
        ))
    }

    private suspend fun seedBonapartes() {
        val gf = dao.insertMember(FamilyMember(
            firstName = "Giuseppe Buonaparte || Đuzepe Buonaparte || Джузеппе Бонапарт || Giuseppe Buonaparte || Giuseppe Buonaparte",
            lastName = "Bonaparte || Bonaparte || Бонапарт || Bonaparte || Bonaparte",
            gender = "MALE", birthDate = "1713", birthPlace = "Ajaccio, Corsica",
            biography = "Grandfather of Napoleon Bonaparte, Corsican high rank citizen. || Deda Napoleona Bonaparte, ugledni rimski imigrant na Korzici. || Дедушка Наполеона Бонапарта, знатный корсиканец. || Großvater Napoleons, vornehmer Korse. || Grand-père de Napoléon Bonaparte, notable d'Ajaccio."
        ))
        val gm = dao.insertMember(FamilyMember(
            firstName = "Maria Saveria || Marija Saveria || Мария Саверия || Maria Saveria || Maria Saveria",
            lastName = "Paravicini || Paravicini || Паравичини || Paravicini || Paravicini",
            gender = "FEMALE", birthDate = "1715", birthPlace = "Ajaccio, Corsica",
            biography = "Corsican noblewoman of Genoese origins. || Korzikanska plemkinja genoveškog porekla. || Корсиканская дворянка генуэзского происхождения. || Adlige genuesischer Herkunft. || Noble d'origine génoise.",
            spouseId = gf
        ))
        dao.updateMember(dao.getMemberById(gf)!!.copy(spouseId = gm))

        val dad = dao.insertMember(FamilyMember(
            firstName = "Carlo Buonaparte || Karlo Buonaparte || Карло Бонапарт || Carlo Buonaparte || Charles Bonaparte",
            lastName = "Bonaparte || Bonaparte || Бонапарт || Bonaparte || Bonaparte",
            gender = "MALE", birthDate = "27.03.1746", birthPlace = "Ajaccio, Corsica",
            biography = "Corsican advocate and legal representative to the court of Louis XVI. || Otac cara Napoleona, advokat i predstavnik Korzike u Parizu. || Юрист и дипломат, отец Наполеона I. || Vater Napoleons, Jurist und Abgeordneter. || Charles Bonaparte, juriste et diplomate, père de Napoléon Ier.",
            fatherId = gf, motherId = gm
        ))
        val mom = dao.insertMember(FamilyMember(
            firstName = "Letizia Ramolino || Leticija Ramolino || Летиция Рамолино || Letizia Ramolino || Letizia Ramolino",
            lastName = "Bonaparte || Bonaparte || Бонапарт || Bonaparte || Bonaparte",
            gender = "FEMALE", birthDate = "24.08.1750", birthPlace = "Ajaccio, Corsica",
            biography = "Madame Mère de l'Empereur, matriarch of the Bonaparte family. || Majka cara, snažna korzikanska žena zvana 'Madame Mère'. || Мадам Мать Императора, сильная женщина своего века. || Die Mutter des Kaisers ('Madame Mère'). || Letizia Bonaparte, 'Madame Mère', matriarche de la famille.",
            spouseId = dad
        ))
        dao.updateMember(dao.getMemberById(dad)!!.copy(spouseId = mom))

        val focus = dao.insertMember(FamilyMember(
            firstName = "Napoleon I || Napoleon I || Наполеон I || Napoleon I || Napoléon Ier",
            lastName = "Bonaparte || Bonaparte || Бонапарт || Bonaparte || Bonaparte",
            gender = "MALE", birthDate = "15.08.1769", birthPlace = "Ajaccio, Corsica",
            biography = "Emperor of the French, dominated European and global affairs for over a decade. || Francuski car, vrhunski vojni genije i reformator modernog građanskog prava. || Император французов, гениальный полководец и реформатор. || Kaiser der Franzosen, einer der genialsten Feldherren. || Empereur des Français, l'un des plus grands génies militaires.",
            fatherId = dad, motherId = mom
        ))
        val spouse = dao.insertMember(FamilyMember(
            firstName = "Marie Louise || Marija Lujza || Мария-Луиза || Marie-Louise || Marie-Louise d'Autriche",
            lastName = "of Austria || od Austrije || Австрийская || von Österreich || d'Autriche",
            gender = "FEMALE", birthDate = "12.12.1791", birthPlace = "Vienna, Austria",
            biography = "Austrian Archduchess, Napoleon's second wife who gave birth to his heir. || Austrijska princeza, druga žena cara i majka kralja Rima. || Австрийская эрцгерцогиня, супруга Наполеона. || Erzherzog von Österreich, Napoleons zweite Ehefrau. || Archiduchesse d'Autriche, seconde épouse de Napoléon.",
            spouseId = focus
        ))
        dao.updateMember(dao.getMemberById(focus)!!.copy(spouseId = spouse))

        // Child: Napoleon II
        dao.insertMember(FamilyMember(
            firstName = "Napoleon II || Napoleon II || Наполеон II || Napoleon II || Napoléon II",
            lastName = "Bonaparte || Bonaparte || Бонапарт || Bonaparte || Bonaparte",
            gender = "MALE", birthDate = "20.03.1811", birthPlace = "Paris, France",
            biography = "The King of Rome, Napoleon's only legitimate son, who died at age 21. || Kralj Rima i vojvoda od Rajhštada, preminuo prerano u 21. godini. || Король Рима, единственный законный сын Наполеона. || König von Rom, verstarb früh mit 21 Jahren. || Édourd-Napoléon, le Roi de Rome, décédé prématurément.",
            fatherId = focus, motherId = spouse
        ))
    }
}
