--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: ir_category; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_category (
    id character varying(20) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(255)
);


ALTER TABLE public.ir_category OWNER TO aggregator;

--
-- Name: ir_category_by_collection; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_category_by_collection (
    id character varying(20) NOT NULL,
    category_id character varying(20) NOT NULL,
    collection_id character varying(20) NOT NULL
);


ALTER TABLE public.ir_category_by_collection OWNER TO aggregator;

--
-- Name: ir_collection; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_collection (
    id character varying(20) NOT NULL,
    name character varying(100) NOT NULL
);


ALTER TABLE public.ir_collection OWNER TO aggregator;

--
-- Name: ir_vertical; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_vertical (
    id character varying(20) NOT NULL,
    name character varying(100),
    description character varying(1000)
);


ALTER TABLE public.ir_vertical OWNER TO aggregator;

--
-- Name: ir_vertical_by_category; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_vertical_by_category (
    vertical_id character varying(20) NOT NULL,
    category_id character varying(20) NOT NULL
);


ALTER TABLE public.ir_vertical_by_category OWNER TO aggregator;

--
-- Name: ir_vertical_by_collection; Type: TABLE; Schema: public; Owner: aggregator; Tablespace: 
--

CREATE TABLE ir_vertical_by_collection (
    id character varying(20) NOT NULL,
    vertical_id character varying(20) NOT NULL,
    collection_id character varying(20) NOT NULL,
    size_factor double precision DEFAULT 1 NOT NULL
);


ALTER TABLE public.ir_vertical_by_collection OWNER TO aggregator;

--
-- Data for Name: ir_category; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_category (id, name, description) FROM stdin;
general	General	Standard web pages
video	Video	Online videos
academic	Academic	Research technical report
photo	Photo/Pictures	Online pictures
jobs	Jobs	Job posts
encyclopedia	Encyclopedia	Encyclopedic entries
travel	Travel	Travel pages
shopping	Shopping	Product shopping page
tech	Tech	Technology pages
health	Health	Health related pages
kids	Kids	Cartoon pages
recipes	Recipes	Recipes page
news	News	News articles
social	Social	Social network pages
books	Books	Book review pages
sports	Sports	Sports pages
blogs	Blogs	Blog articles
games	Games	Electronic game pages
jokes	Jokes	Joke threads
entertainment	Entertainment	Entertainment pages
audio	Audio	Online audios
questions	Q&A	Answers to questions
software	Software	Software downloading pages
local	Local	Local information pages
\.


--
-- Data for Name: ir_category_by_collection; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_category_by_collection (id, category_id, collection_id) FROM stdin;
v001	general	FW14
v002	video	FW14
v003	jobs	FW14
v004	academic	FW14
v005	photo	FW14
v006	encyclopedia	FW14
v007	travel	FW14
v008	shopping	FW14
v009	tech	FW14
v010	health	FW14
v011	kids	FW14
v012	recipes	FW14
v013	news	FW14
v014	social	FW14
v015	books	FW14
v016	sports	FW14
v017	games	FW14
v018	blogs	FW14
v019	jokes	FW14
v020	entertainment	FW14
v021	questions	FW14
v022	audio	FW14
v023	software	FW14
v024	local	FW14
\.


--
-- Data for Name: ir_collection; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_collection (id, name) FROM stdin;
FW13	FedWeb 2013
FW14	FedWeb 2014
\.


--
-- Data for Name: ir_vertical; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_vertical (id, name, description) FROM stdin;
goodreads	Goodread	Goodreads is an Amazon company and "social cataloging" website founded in December 2006 and launched in January 2007 by Otis Chandler, a software engineer and entrepreneur, and Elizabeth Chandler. The website allows individuals to freely search Goodreads' extensive user-populated database of books, annotations, and reviews.
googlebooks	Google Books	Google Books is a service from Google Inc. that searches the full text of books and magazines that Google has scanned, converted to text using optical character recognition, and stored in its digital database.
arxiv	arXiv.org	The arXiv (pronounced "archive", as if the "X" were the Greek letter Chi, χ) is a repository of electronic preprints of scientific papers in the fields of mathematics, physics, astronomy, computer science, quantitative biology, statistics, and quantitative finance, which can be accessed online.
ccsb	CCSB	This is a collection of bibliographies of scientific literature in computer science from various sources, covering most aspects of computer science.
citeseerx	CiteSeerX	Is a public search engine and digital library for scientific and academic papers, primarily in the fields of computer and information science.
citeulike	CiteULike	Is a web service which allows users to save and share citations to academic papers. Based on the principle of social bookmarking, the site works to promote and to develop the sharing of scientific references amongst researchers.
escolarship	eScolarship	eScholarship provides a suite of open access, scholarly publishing services and research tools that enable departments, research units, publishing programs, and individual scholars associated with the University of California to have direct control over the creation and dissemination of the full range of their scholarship.
economists	Economists Online	Open access to both historical and recent economics publications.
cern	CERN Documents	Access articles, reports and multimedia content in HEP.
kfupm	KFUPM ePrints	Research Publication Repository of KFUPM. It offers students, staffs and faculty members the facility of a centralized research repository.
mpra	Munich Personal RePEc Archive\nMunich Personal RePEc Archive\nMunich Personal RePEc Archive\n	This repository is intended to disseminate research papers of economists who want to make their work freely available through the RePEc network but are not affiliated with any institution that provides that furtherance.
msacademic	Microsoft Academic Search	Microsoft Academic Search is an experimental research service developed by Microsoft Research to explore how scholars, scientists, students, and practitioners find academic content, researchers, institutions, and activities.
nature	Nature	Nature is a prominent interdisciplinary scientific journal.
orgprints	Organic Eprints	Organic Eprints is an international open access archive for papers and projects related to research in organic food and farming. The archive contains full-text papers in electronic form together with bibliographic information, abstracts and other metadata. It also offers information on organisations, projects and facilities in the context of organic farming research.
springer	SpringerLink	Springer is a global publishing company that publishes books, e-books and peer-reviewed journals in science, technical and medical (STM) publishing.
utwente	University of Twente Publications	It contains the full text scientific output of the UT, like journal articles, conference papers, books, book sections, and dissertations. Many of these publications are publicly available from this repository.
uab	UAB Digital	UAB is a tool which collects, manages, preserves and disseminates scientific, educational institutions and college at the same time containing digital documents that are part of the collective collections of the UAB libraries completed or that. View a collection organized, open access and interoperable. It contains a great diversity regarding media, subject matter and type of documents.
uq	UQ eSpace	UQ eSpace is the single authoritative source for the research outputs of the staff and students of the University of Queensland and is the archival home of UQ Research Higher Degree Theses.
pubmed	PubMed	PubMed comprises more than 24 million citations for biomedical literature from MEDLINE, life science journals, and online books. Citations may include links to full-text content from PubMed Central and publisher web sites.
lastfm	LastFM	The site offers numerous social networking features and can recommend and play artists similar to the user's favourites; it also features a wiki system analogous to Wikipedia, wherein registered users can collaborate on hyperlinked information about tracks, releases (albums, etc.), artists, bands, tags, and record labels.
lyrics	LYRICSnMUSIC	A Lyrics and Music Search Engine — Song Lyrics, YouTube Videos, Band Bio's, Tour Dates and Guitar Tabs on one page.
dailymotion	Dailymotion	Dailymotion SA is a French video-sharing website on which users can upload, share and view videos. Its headquarters are located in the immeuble Horizons 17 in the 17th arrondissement of Paris.
youtube	YouTube	YouTube is a video-sharing website headquartered in San Bruno, California.
googleblogs	Google Blogs	Google Blog Search was a specialized service of Google used to search blogs.
linkedinblog	LinkedIn Blog	Your source for insights and information about LinkedIn
tumblr	Tumblr	Tumblr is a microblogging platform and social networking website founded by David Karp and owned by Yahoo! Inc.
wordpress	WordPress	WordPress is a free and open source blogging tool and a content management system (CMS).
ncsu	NCSU Library	NCSU Libraries, ranked 38th out of 115 North American research libraries, includes 4.4 million volumes, over 50,000 print and electronic serial subscriptions, more than 20,000 videos and film holdings, and more than 40,000 digital images (as of 2011).
columbus	Columbus Library	This library is one of the most-used library systems in the country and is consistently among the top-ranked large city libraries according to "Hennen’s American Public Library Ratings".
imdb	IMDb	The Internet Movie Database (abbreviated IMDb) is an online database of information related to films, television programs, and video games, taking in actors, production crew, fictional characters, biographies, plot summaries, and trivia.
wikibooks	Wikibooks	Wikibooks is a wiki based Wikimedia project hosted by the Wikimedia Foundation for the creation of free content textbooks and annotated texts that anyone can edit.
wikipedia	Wikipedia	Wikipedia is a free-access, free content Internet encyclopedia, supported and hosted by the non-profit Wikimedia Foundation.
cc	Comedy Central	Funny videos, comedy apps, jokes, news and previews of Comedy Central shows
wikispecies	Wikispecies	Wikispecies is a wiki-based online project supported by the Wikimedia Foundation. Its aim is to create a comprehensive free content catalogue of all species and is directed at scientists, rather than at the general public.
wikitionary	Wikitionary	Wiktionary (a blend of the words wiki and dictionary) is a multilingual, web-based project to create a free content dictionary of all words in all languages. It is available in 158 languages and in Simple English.
eonline	E! Online	E! Online is the online arm of E!, featuring live updates on entertainment news stories; the website includes an online-only entertainment news bulletin titled E! News Now, which is updated each weekday.
entertainmentweekly	Entertainment Weekly	Entertainment Weekly (sometimes abbreviated as EW) is an American magazine, published by Time Inc., that covers film, television, music, Broadway theatre, books and popular culture.
tmz	TMZ	TMZ is a celebrity news website that debuted on November 8, 2005. It was a collaboration between AOL and Telepictures Productions
thesun	The Sun	The Sun is the home of Entertainment with 7 days of reviews celebrity gossip, film reviews, the latest TV news and what is going on in the world of entertainment
addictinggames	AddictingGames	AddictingGames is the largest online games site in the US. We reach over 10 million unique users every month (comScore). We are trailblazers in the casual game territory, developing and distributing innovative, irreverent, addictive online games.
armorgames	Armor Games	Armor Games, formerly Games Of Gondor, is a website based in Irvine, California, that hosts free Flash-based browser games. 
crazymonkeygames	CrazyMonkeyGames.com	CrazyMonkeyGames.com was created to provide a fun, friendly, and entertaining website for everyone to enjoy. Our goal is to become your destination of choice for playing free online games.  What makes us so different than the rest of the online gaming sites out there?
gamenode	GameNode	Big collection of free online games in various categories like arcade, shooting, adventure, puzzles, strategy, sports and more. New free online games are added daily and no registration is required.
gamescom	Games.com	Games.com powers gaming across the Web. We help millions of people discover, play and share games across the globe and provide a best in class distribution and monetization platform for game developers, publishers and advertisers.
miniclip	Miniclip	Miniclip is a mobile and online games company which includes the website Miniclip.com, which was launched in 2001 and is known for having a large and varied collection of games.
aboutcom	About.com	About.com is an Internet-based network of content, providing articles and videos about various topics.
gigablast	Gigablast	Gigablast is a small independent web search engine based in New Mexico.
baidu	Baidu	Baidu offers many services, including a Chinese language-search engine for websites, audio files, and images.
cdc	Centers for Disease Control and Prevention	The Centers for Disease Control and Prevention (CDC) is the national public health institute of the United States. The CDC is a federal agency under the Department of Health and Human Services and is headquartered in unincorporated DeKalb County, Georgia, a few miles northeast of the Atlanta city limits.
healthfinder	healthfinder.org	Your source for reliable health information from the Federal government. Offering quick guides to healthy living, personalized health advice, and tips and tools to help you and those you care about stay healthy.
healthcentral	HealthCentral	HealthCentral.com is one of the most trusted sources of medical information and up to date news and contains a doctor-approved health encyclopedia of diseases and conditions, the ability to find symptoms and treatments. Also drug information with side effects and interactions.
healthline	Healthline	Healthline Networks is a privately owned provider of health information and technology solutions for publishers, advertisers, employers, healthcare providers, and health plans.
healthlinks	Healthlinks.net	A Worldwide Directory for Healthcare Consumers and Professionals Providing links to Health Services and Products, Alternative Health, Education, Dental and Medical Resources, Hospitals, Employment, Healthcare Publications, Mental Health and Much More!
mayoclinic	Mayo Clinic	Mayo Clinic is a nonprofit medical practice and medical research group based in Rochester, Minnesota. It is the first and largest integrated nonprofit medical group practice in the world, employing more than 3,800 physicians and scientists and 50,900 allied health staff.
medicinenet	MedicineNet.com	MedicineNet is a medical website that provides detailed information about diseases, conditions, medications and general health.
medlineplus	MedlinePlus	MedlinePlus is an online information service produced by the United States National Library of Medicine. The service provides curated consumer health information in English and Spanish.
uihc	University of Iowa Hospitals and Clinics	University of Iowa Hospitals and Clinics—recognized as one of the best hospitals in the United States—is Iowa's only comprehensive academic medical center and a regional referral center.
webmd	WebMD	WebMD is an American corporation which provides health information services.
glassdoor	Glassdoor	Glassdoor is an American "job and career site where employees anonymously dish on the pros and cons of their companies and bosses".
jobsite	Jobsite	We help real people find real jobs; connecting the best in talent with the best in recruitment.
linkedinjobs	LinkedIn Jobs	Search for jobs on LinkedIn, and read advice from thought leaders, view presentations, see examples, find experts, and browse other resources on finding a job.
simplyhired	Simply Hired	Simply Hired is an employment website for job listings (thus also an example of vertical search) and online recruitment advertising network.
usajobs	USAJobs	USAJobs (styled USAJOBS) is the United States Government's official website for listing civil service job opportunities with federal agencies.
picasa	Picasa	Picasa is an image organizer and image viewer for organizing and editing digital photos, plus an integrated photo-sharing website
fpnotebook	Family Practice Notebook	The Family Practice Notebook is a medical database focused on family practice.
ccjokes	Comedy Central Jokes.com	Comedy Central Jokes.com - tons of funny jokes to tell &amp; share: dirty jokes, Yo' Mama jokes, sports jokes, funny insults &amp; pick-up lines, Blonde jokes, joke of the day + more
kickassjokes	Kickass Humor	The funniest jokes on the web! Including Chuck Norris, Dirty, Racial, Celebrities, Pick up lines, Comebacks, Yo Momma, Blonde jokes and more!
cartoonnetwork	Cartoon Network	Cartoon Network is an American basic cable and satellite television channel that is owned by the Turner Broadcasting System division of Time Warner. The channel airs mainly animated programming, ranging from action to animated comedy.
disneyfamily	Disney Family.com	Disney Family.com provides answers for every family's needs. Find resources on parenting and raising healthy children, activities, entertainment, recipes, family travel and attractions, budgeting, shopping, coupons, and answers from the experts, other moms.
factmonster	Fact Monster	Fact Monster is a free reference site for students, teachers, and parents. Get homework help and find facts on thousands of subjects, including sports, entertainment, geography, history, biography, education, and health.
kidrex	KidRex	KidRex is a child-safe search engine powered by Google Custom Search. The site utilizes Google SafeSearch and maintains its own database of inappropriate websites and keywords.
kidsclick	KidsClick!	Annotated searchable directory of websites created for kids by librarians. Searchable by subject, reading level and degree of picture content.
nickjr	Nick Jr.	Find free preschool games, full episodes, and kids activities featuring Dora the Explorer, PAW Patrol, Wallykazam, Bubble Guppies, and more of your child&amp;#039;s favorite Nick Jr. shows!
nickelodeon	Nickelodeon	Nickelodeon (often shortened to Nick, and originally called Pinwheel) is an American basic cable and satellite television network. Most of its programming is aimed at children and adolescents ages 8–16, while its weekday morning edutainment programs are targeted at younger children ages 2–8.
oercommons	OER Commons	OER Commons is a freely accessible online library that allows teachers and others to search and discover open educational resources (OER) and other freely available instructional materials.
quinturakids	Quintura for Kids	Quintura for Kids is a visual and intuitive search engine for children
foursquare	Foursquare	Foursquare is a location-based social networking website for mobile devices, such as smartphones.
bbc	BBC	Breaking news, sport, TV, radio and a whole lot more. The BBC informs, educates and entertains - wherever you are, whatever your age.
bingnews	Bing News	Bing News (previously Live Search News) is a part of Microsoft's Bing search engine. It is a search engine and aggregator specifically for news articles through a variety of trusted and credible internet news sources, including New York Times, Washington Post and Reuters.
chroniclingamerica	Chronicling America	Chronicling America, begun in 2005, is a database and companion website produced by the National Digital Newspaper Program (NDNP), a partnership between the Library of Congress and the National Endowment for the Humanities, maintained by the LOC. The Chronicling America website contains digitized newspaper pages and information about historic newspapers to place the primary sources in context and support future research.
cnn	CNN.com	CNN.com International delivers breaking news from across the globe and information on the latest top stories, business, sports and entertainment headlines. Follow the news as it happens through: special reports, videos, audio, photo galleries plus interactive maps and timelines.
forbes	Forbes.com	Forbes is a leading source for reliable business news and financial information. Read news, politics, economics, business & finance on Forbes.com.
googlenews	Google News	Comprehensive up-to-date news coverage, aggregated from sources all over the world by Google News.
jsonline	Journal Sentinel Online	Milwaukee and Wisconsin News, Sports, Weather, Business, Opinion, Photos, Video, and Investigative Reporting from Wisconsin's Number One News Source
slate	Slate Magazine	Slate is a United States English language online current affairs and culture magazine
theguardian	The Guardian	Latest news, sport, business, comment, analysis and reviews from the Guardian, the world's leading liberal voice
thestreet	The Street	Follow the stock market today on TheStreet. Get business news that moves markets, award-winning stock analysis, market data and stock trading ideas.
washingtonpost	Washington Post	Breaking news and analysis on politics, business, world national news, entertainment more. In-depth DC, Virginia, Maryland news coverage including traffic, weather, crime, education, restaurant reviews and more.
hnsearch	HN Search	Hacker News Search, millions articles and comments at your fingertips.
slashdot	Slashdot	Slashdot (sometimes abbreviated as /.) is a technology-related news website owned by the US-based company Dice Holdings, Inc. The site, which bills itself as "News for Nerds. Stuff that Matters", features user-submitted and evaluated news stories about science and technology-related topics.
theregister	The Register	Independent news, views, opinions and reviews on the latest in the IT industry.
devianart	devianART	Art - community of artists and those devoted to art. Digital art, skin art, themes, wallpaper art, traditional art, photography, poetry / prose. Art prints.
flickr	Flickr	Flickr is almost certainly the best online photo management and sharing application in the world. Show off your favorite photos and videos to the world, securely and privately show content to your friends and family, or blog the photos and videos you take with a cameraphone.
fotolia	Fotolia	More than 20 million cheap royalty free images, vectors, videos. Fotolia the image bank for your publishing and marketing projects!
gettyimages	Getty Images	Find high resolution royalty-free images, editorial stock photos, vector art, video footage clips and stock music licensing at the richest image search photo library online
iconfinder	Iconfinder	Search through more than 320,000 free and premium icons in an easy and efficient way.
nypl	NYPL Digital Gallery	NYPL Digital Gallery provides free and open access to over 800,000 images digitized from the The New York Public Library's vast collections, including illuminated manuscripts, historical maps, vintage posters, rare prints, photographs and more.
openclipart	Openclipart	Clipart for your needs in 2014. Highest quality, free for commercial and non-commercial use on cards, books, crafts, fashion, merchandise, and services.
photobucket	Photobucket	Get free image hosting, easy photo sharing, and photo editing. Upload pictures and videos, create with the online photo editor, or browse a photo gallery or album.
picsearch	Picsearch	The unrivalled search engine for pictures, images and animations.
github	GitHub	GitHub is the best place to build software together. Over 4 million people use GitHub to share code.
wikimedia	Wikimedia Commons	Wikimedia Commons (or simply Commons) is an online repository of free-use images, sound, and other media files. It is a project of the Wikimedia Foundation.
funnyordie	Funny or Die	Funny or Die | funny videos, funny video clips, funny pictures - Funny videos, funny pictures, funny jokes, top ten lists, caption contests, and funny articles featuring celebrities, comedians, and you.
4shared	4shared	4shared is a perfect place to store your pictures, documents, videos and files, so you can share them with friends, family, and the world. Claim your free 15GB now!
allexperts	AllExperts	More than 2 million questions answered! Allexperts.com is the oldest & largest free Q&A service on the Internet.
answerscom	Answers.com	The Most Trusted Place for Answering Life's Questions.
chacha	ChaCha	A fusion of computer technology and human intelligence. Live human guides are available to provide additional assistance in finding relevant results.
stackoverflow	Stack Overflow	Stack Overflow is a question and answer site for professional and enthusiast programmers. It's 100% free, no registration required.
yahooanswers	Yahoo! Answers	Yahoo! Answers (formerly known as Yahoo! Q & A) is a community-driven question-and-answer (Q&A) site or a knowledge market launched by Yahoo! on June 28, 2005 that allows users to both submit questions to be answered and answer questions asked by other users.
metaoptimize	MetaOptimize Q+A	Where scientists ask and answer questions on machine learning, natural language processing, artificial intelligence, text analysis, information retrieval, search, and others.
howstuffworks	HowStuffWorks	HowStuffWorks explains thousands of topics, from engines to lock-picking to ESP, with video and illustrations so you can learn how everything works.
allrecipes	Allrecipes	Allrecipes is the #1 place for recipes, cooking tips, and how-to food videos—all rated and reviewed by millions of home cooks. Allrecipes makes it easy to find everyday recipes for chicken, make the perfect birthday cake, or plan your next holiday dinner.
cookingcom	Cooking.com	Kitchenware and cooking recipes for everyone who loves cooking. Shop top rated cookware, bakeware, cutlery, or find your favorite cooking recipes
foodnetwork	Food Network	Love Food Network shows, chefs and recipes? Find the best recipe ideas, videos, healthy eating advice, party ideas and cooking techniques from top chefs, shows and experts.
foodcom	Food.com	Food.com has a massive collection of recipes that are submitted, rated and reviewed by people who are passionate about food. From international cuisines to quick and easy meal ideas, Food.com is where you can find what youre craving.
mealscom	Meals.com	Get quick and easy recipes using your favorite Nestlé products." /><meta name="keywords" content="Recipes, Meals.com, Simple, Inspiring, Delicious, Seasonal, Nutrition, Health, Healthy Recipes, easy meals, quick meals, planning, dinner, cooking, quick recipes, free online recipes, quick family recipes, best meals, my meals, prepare ahead meals weekly meal planning, special diet, diabetic recipes, vegan recipes, vegetarian recipes, soup, cookies in a jar, easy recipes.
amazon	Amazon.com	Online shopping from the earth&#39;s biggest selection of books, magazines, music, DVDs, videos, electronics, computers, software, apparel &amp; accessories, shoes, jewelry, tools &amp; hardware, housewares, furniture, sporting goods, beauty &amp; personal care, broadband &amp; dsl, gourmet food &amp; just about anything else.
asos	ASOS	Discover the latest in women's fashion and men's clothing online. Shop from over 40,000 styles, including dresses, jeans, shoes and accessories from ASOS and over 800 brands. ASOS brings you the best fashion clothes online.
craigslist	craigslist	craigslist provides local classifieds and forums for jobs, housing, for sale, personals, services, local community and events.
ebay	eBay	Buy and sell electronics, cars, fashion apparel, collectibles, sporting goods, digital cameras, baby items, coupons, and everything else on eBay, the world's online marketplace.
overstock	Overstock.com	Let Overstock.com help you discover designer brands and home goods at the lowest prices online. See for yourself why shoppers love our selection and award-winning customer service.
powells	Powell's	Powell's Books is the largest independent used and new bookstore in the world. We carry an extensive collection of out of print rare, and technical titles as well as many other new and used books in every field.
pronto	Pronto.com	We have products from thousands of stores so you can find the best sales and best prices on tons of brand name bargains. Shop online and get the best discount prices with Pronto.com
target	Target	Spend $50 and get free shipping on over 500K items. Choose from a wide selection of furniture, kids & baby, electronics, toys, shoes & more.
yahooshopping	Yahoo! Shopping	Yahoo! Shopping is the best place to read user reviews, explore great products and buy online.
myspace	Myspace	Myspace is a social networking service with a strong music emphasis owned by Specific Media LLC and pop music singer and actor Justin Timberlake.
reddit	Reddit	Reddit is an entertainment, social networking service and news website where registered community members can submit content, such as text posts or direct links.
tweepz	Tweepz	Twitter is a fairly boring place if there's nobody interesting you are following - but it's always been an issue to find interesting people. That's where tweepz comes in! Think of us as the whitepages for twitter. Search for twitter accounts based on names, locations and keywords.
cnetdownload	CNET Download.com	Come to CNET Download.com for free and safe Download downloads and reviews including Browsers, Business Software, Communications and many more.
sourceforge	SourceForge	Resources for open-source developers and a directory of in-development open-source software.
bleacherreport	Bleacher Report	From BleacherReport.com, your destination for the latest news on your teams and topics in sports.
espn	ESPN	ESPN.com provides comprehensive sports coverage.  Complete sports information including NFL, MLB, NBA, College Football, College Basketball scores and news.
foxsports	FOX Sports	Find live scores, player & team news, videos, rumors, stats, standings, schedules & fantasy games on FOX Sports.
nba	NBA.com	The official site of the National Basketball Association. Includes news, features, multimedia, player profiles, chat transcripts, schedules and statistics.
nhl	NHL.com	The official National Hockey League web site includes features, news, rosters, statistics, schedules, teams, live game radio broadcasts, and video clips.
sbnation	SB Nation	SB Nation is a collection of over 300 individual communities, each offering high quality year-round coverage and conversation led by fans who are passionate about their favorite teams, leagues or sports. By empowering fans, SB Nation has become the largest and fastest growing grassroots sports network.
sportingnews	Sporting News	Sporting News is a one-stop shop for avid fans like you to follow breaking sports news and major stories.
wwe	WWE	WWE, the recognized leader in global sports-entertainment, featuring the unrivaled Superstars of the ring including John Cena, Randy Orton, The Rock, CM Punk, Triple H and The Undertaker, as well as WWE Divas and Legends.
arstechnica	Ars Technica	Serving the Technologist for more than a decade. IT news, reviews, and analysis.
cnet	CNET	CNET is the world's leader in tech product reviews, news, prices, videos, forums, how to's and more.
technet	TechNet	TechNet is the home for all resources and tools designed to help IT professionals succeed with Microsoft products and technologies.
technorati	Technorati	We shape the conversation of online publishing, advertising technology, content monetization and website marketing strategies.
techrepublic	TechRepublic	Providing IT professionals with a unique blend of original content, peer-to-peer advice from the largest community of IT leaders on the Web.
tripadvisor	TripAdvisor	Unbiased hotel reviews, photos and travel advice for hotels and vacations - Compare prices with just one click.
wikitravel	Wikitravel	Open source travel guide featuring up-to-date information on attractions hotels restaurants travel tips and more.  Free and reliable advice written by Wikitravellers from around the globe.
5min	5min.com	AOL On is the web's largest curated library of premium video, and the home of AOL Originals. We feature the latest and hottest videos in News, Entertainment, Tech, Lifestyle...
aolvideo	AOL Video	AOL offers today's news, sports, stock quotes, weather, movie reviews, TV trends and more. Get free email, AIM access, online radio, videos and horoscopes -- all on AOL.com!
googlevideos	Google Videos	Google Videos is a video search engine from Google. It was formerly a free video-sharing website and allowed selected videos to be remotely embedded on other websites and provided the necessary HTML code alongside the media, similar to YouTube.
mefeedia	MeFeedia	Video search & discovery on Web, Mobile, Tablet, and TV.
metacafe	Metacafe	One of the world's largest video sites, serving the best videos, funniest movies and clips.
nationalgeographic	National Geographic	National Geographic provides free maps, photos, videos and daily news stories, as well as articles and features about animals, the environment, cultures, history, world music, and travel.
veoh	Veoh	Veoh is the premier watch movies online provider that you and your whole family are sure to love. Upload your favorites and share them with friends. Register your online movies account today!
vimeo	Vimeo	Vimeo is the home for high-quality videos and the people who love them.
yahooscreen	Yahoo! Screen	Watch videos online for free on Yahoo! Screen. Find sports, kids and funny videos including the latest news video clips.
bigweb	Big Web	General web search engines.
ask	Ask.com	Ask.com (originally known as Ask Jeeves) is a question answering-focused web search engine founded in 1995 by Garrett Gruener and David Warthen in Berkeley, California.
cmu	CMU ClueWeb	The ClueWeb09 dataset was created to support research on information retrieval and related human language technologies. It consists of about 1 billion web pages in ten languages that were collected in January and February 2009. The dataset is used by several tracks of the TREC conference.
\.


--
-- Data for Name: ir_vertical_by_category; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_vertical_by_category (vertical_id, category_id) FROM stdin;
arxiv	academic
ccsb	academic
cern	academic
citeseerx	academic
citeulike	academic
economists	academic
escolarship	academic
kfupm	academic
mpra	academic
msacademic	academic
nature	academic
orgprints	academic
springer	academic
utwente	academic
uab	academic
uq	academic
pubmed	academic
pubmed	health
lastfm	audio
lyrics	audio
dailymotion	audio
dailymotion	video
youtube	audio
youtube	video
googleblogs	blogs
linkedinblog	blogs
tumblr	blogs
wordpress	blogs
goodreads	books
googlebooks	books
columbus	books
ncsu	books
ncsu	academic
imdb	encyclopedia
wikibooks	encyclopedia
wikipedia	encyclopedia
wikispecies	encyclopedia
wikitionary	encyclopedia
eonline	entertainment
entertainmentweekly	entertainment
tmz	entertainment
thesun	entertainment
thesun	news
thesun	sports
addictinggames	games
armorgames	games
crazymonkeygames	games
gamenode	games
gamescom	games
miniclip	games
aboutcom	general
aboutcom	encyclopedia
gigablast	general
baidu	general
cdc	health
healthfinder	health
healthcentral	health
healthline	health
healthlinks	health
mayoclinic	health
medicinenet	health
medlineplus	health
uihc	health
webmd	health
glassdoor	jobs
jobsite	jobs
linkedinjobs	jobs
simplyhired	jobs
usajobs	jobs
cc	audio
cc	video
ccjokes	jokes
kickassjokes	jokes
cartoonnetwork	kids
disneyfamily	kids
factmonster	kids
kidrex	kids
kidsclick	kids
nickjr	kids
nickelodeon	kids
oercommons	kids
oercommons	encyclopedia
quinturakids	kids
foursquare	local
bbc	news
bingnews	news
chroniclingamerica	news
cnn	news
forbes	news
googlenews	news
jsonline	news
slate	news
theguardian	news
thestreet	news
washingtonpost	news
hnsearch	news
hnsearch	tech
slashdot	news
slashdot	tech
theregister	news
theregister	tech
devianart	photo
flickr	photo
fotolia	photo
gettyimages	photo
iconfinder	photo
nypl	photo
openclipart	photo
photobucket	photo
picasa	photo
picsearch	photo
wikimedia	photo
funnyordie	video
funnyordie	photo
4shared	audio
4shared	video
4shared	books
4shared	photo
allexperts	questions
answerscom	questions
chacha	questions
stackoverflow	questions
yahooanswers	questions
metaoptimize	questions
metaoptimize	academic
howstuffworks	questions
howstuffworks	kids
allrecipes	recipes
cookingcom	recipes
foodnetwork	recipes
foodcom	recipes
mealscom	recipes
amazon	shopping
asos	shopping
craigslist	shopping
ebay	shopping
overstock	shopping
powells	shopping
pronto	shopping
target	shopping
yahooshopping	shopping
myspace	social
reddit	social
tweepz	social
cnetdownload	software
github	software
sourceforge	software
bleacherreport	sports
espn	sports
foxsports	sports
nba	sports
nhl	sports
sbnation	sports
sportingnews	sports
wwe	sports
arstechnica	tech
cnet	tech
technet	tech
technorati	tech
techrepublic	tech
tripadvisor	travel
wikitravel	travel
5min	video
aolvideo	video
googlevideos	video
fpnotebook	health
cmu	general
mefeedia	video
metacafe	video
nationalgeographic	video
veoh	video
vimeo	video
yahooscreen	video
bigweb	general
ask	general
\.


--
-- Data for Name: ir_vertical_by_collection; Type: TABLE DATA; Schema: public; Owner: aggregator
--

COPY ir_vertical_by_collection (id, vertical_id, collection_id, size_factor) FROM stdin;
e001	arxiv	FW14	1
e002	ccsb	FW14	1
e003	cern	FW14	1
e064	fpnotebook	FW14	1
e004	citeseerx	FW14	1
e005	citeulike	FW14	1
e007	escolarship	FW14	1
e008	kfupm	FW14	1
e009	mpra	FW14	1
e010	msacademic	FW14	1
e011	nature	FW14	1
e012	orgprints	FW14	1
e013	springer	FW14	1
e014	utwente	FW14	1
e015	uab	FW14	1
e016	uq	FW14	1
e017	pubmed	FW14	1
e018	lastfm	FW14	1
e019	lyrics	FW14	1
e055	cmu	FW14	1
d021	dailymotion	FW14	1
e022	youtube	FW14	1
e023	googleblogs	FW14	1
e024	linkedinblog	FW14	1
e025	tumblr	FW14	1
e026	wordpress	FW14	1
e028	goodreads	FW14	1
e029	googlebooks	FW14	1
e030	ncsu	FW14	1
e032	imdb	FW14	1
e033	wikibooks	FW14	1
e034	wikipedia	FW14	1
e036	wikispecies	FW14	1
e037	wikitionary	FW14	1
e038	eonline	FW14	1
e039	entertainmentweekly	FW14	1
e041	tmz	FW14	1
e043	addictinggames	FW14	1
e044	armorgames	FW14	1
e045	crazymonkeygames	FW14	1
e047	gamenode	FW14	1
e048	gamescom	FW14	1
e049	miniclip	FW14	1
e050	aboutcom	FW14	1
e057	gigablast	FW14	1
e062	baidu	FW14	1
e063	cdc	FW14	1
e065	healthfinder	FW14	1
e066	healthcentral	FW14	1
e067	healthline	FW14	1
e068	healthlinks	FW14	1
e070	mayoclinic	FW14	1
e071	medicinenet	FW14	1
e072	medlineplus	FW14	1
e075	uihc	FW14	1
e076	webmd	FW14	1
e077	glassdoor	FW14	1
e078	jobsite	FW14	1
e079	linkedinjobs	FW14	1
e080	simplyhired	FW14	1
e081	usajobs	FW14	1
e020	cc	FW14	1
e082	ccjokes	FW14	1
e083	kickassjokes	FW14	1
e087	factmonster	FW14	1
e085	cartoonnetwork	FW14	1
e086	disneyfamily	FW14	1
e088	kidrex	FW14	1
e004	citeseerx	FW13	788.702199999999948
e006	economists	FW13	1291.95000000000005
e007	escolarship	FW13	675.562199999999962
e008	kfupm	FW13	71.4736000000000047
e009	mpra	FW13	429.024000000000001
e010	msacademic	FW13	555.458399999999983
e011	nature	FW13	178.380799999999994
e012	orgprints	FW13	254.733499999999992
e013	springer	FW13	792.349600000000009
e014	utwente	FW13	213.367500000000007
e015	uab	FW13	289.806899999999985
e016	uq	FW13	446.889999999999986
e017	pubmed	FW13	823.184999999999945
e018	lastfm	FW13	551.285999999999945
e019	lyrics	FW13	423.503600000000006
e020	cc	FW13	141.475999999999999
e022	youtube	FW13	2091.26540000000023
e023	googleblogs	FW13	1015.38919999999996
e024	linkedinblog	FW13	36.3924000000000021
e025	tumblr	FW13	2255.45319999999992
e026	wordpress	FW13	579.671100000000024
e027	columbus	FW13	905.421900000000051
e028	goodreads	FW13	239.290999999999997
e029	googlebooks	FW13	1496.20059999999989
e030	ncsu	FW13	1023.76559999999995
e032	imdb	FW13	675.832400000000007
e033	wikibooks	FW13	218.501000000000005
e034	wikipedia	FW13	460.240000000000009
e036	wikispecies	FW13	360.180000000000007
e038	eonline	FW13	223.967700000000008
e039	entertainmentweekly	FW13	176.176700000000011
e041	tmz	FW13	119.687399999999997
e042	thesun	FW13	112.685599999999994
e043	addictinggames	FW13	192.585900000000009
e044	armorgames	FW13	5.06299999999999972
e045	crazymonkeygames	FW13	57.1064000000000007
e047	gamenode	FW13	53.7120000000000033
e048	gamescom	FW13	163.384800000000013
e049	miniclip	FW13	72.272199999999998
e050	aboutcom	FW13	707.676600000000008
e055	cmu	FW13	607.333999999999946
e057	gigablast	FW13	354.139499999999998
e062	baidu	FW13	2486.20240000000013
e063	cdc	FW13	241.413399999999996
e065	healthfinder	FW13	31.5120000000000005
e066	healthcentral	FW13	171.996000000000009
e067	healthline	FW13	208.994
e068	healthlinks	FW13	180.56880000000001
e070	mayoclinic	FW13	146.362799999999993
e071	medicinenet	FW13	181.166699999999992
e072	medlineplus	FW13	5.10400000000000009
e075	uihc	FW13	72.0799999999999983
e076	webmd	FW13	158.888800000000003
e077	glassdoor	FW13	201.070699999999988
e078	jobsite	FW13	223.168000000000006
e079	linkedinjobs	FW13	224.034400000000005
e080	simplyhired	FW13	183.672300000000007
e082	ccjokes	FW13	140.832999999999998
e083	kickassjokes	FW13	27.0503999999999998
e085	cartoonnetwork	FW13	15.3765000000000001
e086	disneyfamily	FW13	216.972000000000008
e087	factmonster	FW13	675.700799999999958
e088	kidrex	FW13	1100.59500000000003
e002	ccsb	FW13	1419.5
e003	cern	FW13	184.577400000000011
e089	kidsclick	FW14	1
e090	nickjr	FW14	1
e092	oercommons	FW14	1
e093	quinturakids	FW14	1
e095	foursquare	FW14	1
e098	bbc	FW14	1
e100	chroniclingamerica	FW14	1
e101	cnn	FW14	1
e102	forbes	FW14	1
e104	jsonline	FW14	1
e106	slate	FW14	1
e108	thestreet	FW14	1
e109	washingtonpost	FW14	1
e110	hnsearch	FW14	1
e111	slashdot	FW14	1
e112	theregister	FW14	1
e113	devianart	FW14	1
e052	ask	FW14	1
e114	flickr	FW14	1
e115	fotolia	FW14	1
e117	gettyimages	FW14	1
e118	iconfinder	FW14	1
e119	nypl	FW14	1
e120	openclipart	FW14	1
e121	photobucket	FW14	1
e122	picasa	FW14	1
e123	picsearch	FW14	1
e124	wikimedia	FW14	1
e126	funnyordie	FW14	1
e127	4shared	FW14	1
e128	allexperts	FW14	1
e129	answerscom	FW14	1
e130	chacha	FW14	1
e131	stackoverflow	FW14	1
e132	yahooanswers	FW14	1
e133	metaoptimize	FW14	1
e134	howstuffworks	FW14	1
e135	allrecipes	FW14	1
e136	cookingcom	FW14	1
e137	foodnetwork	FW14	1
e138	foodcom	FW14	1
e139	mealscom	FW14	1
e140	amazon	FW14	1
e185	yahooscreen	FW14	1
e141	asos	FW14	1
e142	craigslist	FW14	1
e143	ebay	FW14	1
e144	overstock	FW14	1
e145	powells	FW14	1
e146	pronto	FW14	1
e147	target	FW14	1
e148	yahooshopping	FW14	1
e152	myspace	FW14	1
e153	reddit	FW14	1
e154	tweepz	FW14	1
e156	cnetdownload	FW14	1
e157	github	FW14	1
e158	sourceforge	FW14	1
e159	bleacherreport	FW14	1
e160	espn	FW14	1
e161	foxsports	FW14	1
e184	vimeo	FW14	1
e170	technorati	FW14	1
e163	nhl	FW14	1
e164	sbnation	FW14	1
e165	sportingnews	FW14	1
e092	oercommons	FW13	151.593400000000003
e093	quinturakids	FW13	188.809799999999996
e095	foursquare	FW13	207.829599999999999
e098	bbc	FW13	201.597000000000008
e099	bingnews	FW13	280.869799999999998
e100	chroniclingamerica	FW13	19.0212000000000003
e101	cnn	FW13	93.6606000000000023
e102	forbes	FW13	2039.14799999999991
e103	googlenews	FW13	1215.38270000000011
e104	jsonline	FW13	192.942399999999992
e106	slate	FW13	97.5091000000000037
e107	theguardian	FW13	290.159699999999987
e108	thestreet	FW13	137.615399999999994
e110	hnsearch	FW13	134.127999999999986
e111	slashdot	FW13	162.906800000000004
e112	theregister	FW13	111.642300000000006
e113	devianart	FW13	1033.91470000000004
e114	flickr	FW13	1310.16000000000008
e115	fotolia	FW13	321.352199999999982
e117	gettyimages	FW13	116.522999999999996
e118	iconfinder	FW13	146.383199999999988
e119	nypl	FW13	2578.76879999999983
e120	openclipart	FW13	249.408800000000014
e121	photobucket	FW13	342.393500000000017
e122	picasa	FW13	258.960300000000018
e123	picsearch	FW13	490.019999999999982
e124	wikimedia	FW13	761.282500000000027
e126	funnyordie	FW13	123.849000000000004
e127	4shared	FW13	266.323599999999999
e128	allexperts	FW13	524.46040000000005
e130	chacha	FW13	296.189300000000003
e131	stackoverflow	FW13	327.376800000000003
e132	yahooanswers	FW13	897.56370000000004
e133	metaoptimize	FW13	41.254800000000003
e134	howstuffworks	FW13	190.69919999999999
e135	allrecipes	FW13	76.7438999999999965
e136	cookingcom	FW13	34.740000000000002
e137	foodnetwork	FW13	72.4932000000000016
e138	foodcom	FW13	54.9288000000000025
e139	mealscom	FW13	37.7239999999999966
e140	amazon	FW13	357.271200000000022
e141	asos	FW13	91.0892000000000053
e142	craigslist	FW13	422.666299999999978
e143	ebay	FW13	202.71520000000001
e145	powells	FW13	282.881599999999992
e146	pronto	FW13	187.147999999999996
e147	target	FW13	457.617599999999982
e148	yahooshopping	FW13	315.343200000000024
e152	myspace	FW13	121.159999999999997
e153	reddit	FW13	180.453599999999994
e154	tweepz	FW13	748.228799999999978
e156	cnetdownload	FW13	231.641999999999996
e157	github	FW13	667.010300000000029
e158	sourceforge	FW13	306.097100000000012
e159	bleacherreport	FW13	113.384699999999995
e160	espn	FW13	219.17519999999999
e161	foxsports	FW13	61.1037000000000035
e162	nba	FW13	18.2805
e163	nhl	FW13	72.215999999999994
e165	sportingnews	FW13	88.1075000000000017
e166	wwe	FW13	10.4857999999999993
e170	technorati	FW13	172.859399999999994
e184	vimeo	FW13	477.921600000000012
e185	yahooscreen	FW13	923.366399999999999
e089	kidsclick	FW13	190.062600000000003
e090	nickjr	FW13	26.6707999999999998
e166	wwe	FW14	1
e167	arstechnica	FW14	1
e168	cnet	FW14	1
e169	technet	FW14	1
e171	techrepublic	FW14	1
e172	tripadvisor	FW14	1
e173	wikitravel	FW14	1
e174	5min	FW14	1
e175	aolvideo	FW14	1
e182	veoh	FW14	1
e176	googlevideos	FW14	1
e178	mefeedia	FW14	1
e179	metacafe	FW14	1
e200	bigweb	FW14	1
e181	nationalgeographic	FW14	1
e001	arxiv	FW13	576.705199999999991
e005	citeulike	FW13	659.916600000000017
e021	dailymotion	FW13	996.801399999999944
e037	wikitionary	FW13	1448.03700000000003
e052	ask	FW13	1351.25759999999991
e064	fpnotebook	FW13	185.419999999999987
e081	usajobs	FW13	42.7349999999999994
e091	nickelodeon	FW13	103.311000000000007
e109	washingtonpost	FW13	123.748800000000003
e129	answerscom	FW13	293.796199999999999
e144	overstock	FW13	212.969999999999999
e164	sbnation	FW13	202.220100000000002
e167	arstechnica	FW13	99.533600000000007
e168	cnet	FW13	109.971999999999994
e169	technet	FW13	346.185000000000002
e171	techrepublic	FW13	234.18950000000001
e172	tripadvisor	FW13	268.903399999999976
e173	wikitravel	FW13	86.1650000000000063
e174	5min	FW13	137.788000000000011
e175	aolvideo	FW13	1072.55719999999997
e176	googlevideos	FW13	755.123100000000022
e178	mefeedia	FW13	981.642000000000053
e179	metacafe	FW13	426.618400000000008
e181	nationalgeographic	FW13	144.874799999999993
e182	veoh	FW13	588.419999999999959
e200	bigweb	FW13	1645.4387999999999
\.


--
-- Name: ir_category_by_collection_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_category_by_collection
    ADD CONSTRAINT ir_category_by_collection_pk PRIMARY KEY (category_id, collection_id);


--
-- Name: ir_category_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_category
    ADD CONSTRAINT ir_category_pk PRIMARY KEY (id);


--
-- Name: ir_collection_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_collection
    ADD CONSTRAINT ir_collection_pk PRIMARY KEY (id);


--
-- Name: ir_vertical_by_category_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical_by_category
    ADD CONSTRAINT ir_vertical_by_category_pk PRIMARY KEY (vertical_id, category_id);


--
-- Name: ir_vertical_by_collection_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical_by_collection
    ADD CONSTRAINT ir_vertical_by_collection_pk PRIMARY KEY (vertical_id, collection_id);


--
-- Name: ir_vertical_pk; Type: CONSTRAINT; Schema: public; Owner: aggregator; Tablespace: 
--

ALTER TABLE ONLY ir_vertical
    ADD CONSTRAINT ir_vertical_pk PRIMARY KEY (id);


--
-- Name: ir_category_by_collection_category_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_category_by_collection
    ADD CONSTRAINT ir_category_by_collection_category_id_fk FOREIGN KEY (category_id) REFERENCES ir_category(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ir_category_by_collection_collection_id; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_category_by_collection
    ADD CONSTRAINT ir_category_by_collection_collection_id FOREIGN KEY (collection_id) REFERENCES ir_collection(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ir_vertical_by_category_category_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_vertical_by_category
    ADD CONSTRAINT ir_vertical_by_category_category_id_fk FOREIGN KEY (category_id) REFERENCES ir_category(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ir_vertical_by_category_vertical_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_vertical_by_category
    ADD CONSTRAINT ir_vertical_by_category_vertical_id_fk FOREIGN KEY (vertical_id) REFERENCES ir_vertical(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ir_vertical_by_collection_collection_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_vertical_by_collection
    ADD CONSTRAINT ir_vertical_by_collection_collection_id_fk FOREIGN KEY (collection_id) REFERENCES ir_collection(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: ir_vertical_by_collection_vertical_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: aggregator
--

ALTER TABLE ONLY ir_vertical_by_collection
    ADD CONSTRAINT ir_vertical_by_collection_vertical_id_fk FOREIGN KEY (vertical_id) REFERENCES ir_vertical(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

