package com.example.data.database

import com.example.data.entity.CategoryEntity
import com.example.data.entity.ChapterEntity
import com.example.data.entity.ExtractionStatus
import com.example.data.entity.LawCategoryCrossRef
import com.example.data.entity.LawEntity
import com.example.data.entity.SectionEntity
import com.example.data.entity.SubsectionEntity
import com.example.data.entity.VerificationStatus
import com.example.data.importer.LegalTextParser
import com.example.utils.HashUtils

object LawPreloader {

    fun getInitialCategories(): List<CategoryEntity> {
        return listOf(
            CategoryEntity(1, "বাংলাদেশের সংবিধান", "The Constitution of Bangladesh", "Gavel", 1),
            CategoryEntity(2, "দণ্ডবিধি ও ফৌজদারি আইন", "Penal Code & Criminal Laws", "Shield", 2),
            CategoryEntity(3, "ফৌজদারি কার্যবিধি", "Code of Criminal Procedure (CrPC)", "MenuBook", 3),
            CategoryEntity(4, "সাক্ষ্য আইন", "Evidence Act", "FactCheck", 4),
            CategoryEntity(5, "দেওয়ানি কার্যবিধি", "Code of Civil Procedure (CPC)", "Balance", 5),
            CategoryEntity(6, "নারী ও শিশু বিষয়ক আইন", "Women & Children Protection Laws", "People", 6),
            CategoryEntity(7, "মাদকদ্রব্য সংক্রান্ত আইন", "Narcotics Control Laws", "Warning", 7),
            CategoryEntity(8, "সাইবার ও তথ্যপ্রযুক্তি আইন", "Cyber & ICT Laws", "Security", 8),
            CategoryEntity(9, "ভূমি ও সম্পত্তি আইন", "Land & Property Laws", "HomeWork", 9),
            CategoryEntity(10, "পারিবারিক আইন", "Family & Marriage Laws", "Favorite", 10),
            CategoryEntity(11, "শ্রম ও কর্মসংস্থান আইন", "Labor & Employment Laws", "Engineering", 11),
            CategoryEntity(12, "দুর্নীতি ও অর্থনৈতিক অপরাধ", "Anti-Corruption & Financial Crime", "AccountBalance", 12),
            CategoryEntity(13, "অস্ত্র ও বিস্ফোরক সংক্রান্ত আইন", "Arms & Explosives Laws", "LocalPolice", 13),
            CategoryEntity(14, "মানব পাচার ও মানবাধিকার", "Human Trafficking & Human Rights", "VolunteerActivism", 14),
            CategoryEntity(15, "সড়ক পরিবহন আইন", "Road Transport Laws", "DirectionsCar", 15),
            CategoryEntity(16, "কোম্পানি ও ব্যবসা সংক্রান্ত আইন", "Company & Commercial Laws", "Business", 16),
            CategoryEntity(17, "নির্বাচন সংক্রান্ত আইন", "Election Laws", "HowToVote", 17),
            CategoryEntity(18, "পরিবেশ সংক্রান্ত আইন", "Environmental Laws", "Eco", 18),
            CategoryEntity(19, "কর ও রাজস্ব সংক্রান্ত আইন", "Tax & Revenue Laws", "ReceiptLong", 19),
            CategoryEntity(20, "অন্যান্য বিশেষ আইন", "Other Special Laws", "AutoStories", 20)
        )
    }

    fun getInitialLaws(): List<LawEntity> {
        return listOf(
            LawEntity(
                id = 1,
                actNumber = "সংবিধান",
                actYear = 1972,
                titleBn = "গণপ্রজাতন্ত্রী বাংলাদেশের সংবিধান",
                titleEn = "The Constitution of the People's Republic of Bangladesh",
                shortDescriptionBn = "বাংলাদেশের সর্বোচ্চ আইন যা রাষ্ট্রপরিচালনার মূলনীতি, মৌলিক অধিকার এবং শাসনব্যবস্থা নির্ধারণ করে।",
                fullIntroductionBn = "আমরা, বাংলাদেশের জনগণ, ১৯৭১ খ্রিষ্টাব্দের মার্চ মাসের ২৬ তারিখে স্বাধীনতা ঘোষণা করিয়া ঐতিহাসিক সংগ্রামের মাধ্যমে স্বাধীন ও সার্বভৌম গণপ্রজাতন্ত্রী বাংলাদেশ প্রতিষ্ঠিত করিয়াছি। সংবিধান প্রজাতন্ত্রের সর্বোচ্চ আইন এবং অন্য কোনো আইন যদি এই সংবিধানের সহিত অসামঞ্জস্য হয়, তাহা হইলে সেই আইনের যতখানি অসামঞ্জস্যপূর্ণ, ততখানি বাতিল হইবে (অনুচ্ছেদ ৭)।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-367.html",
                status = "কার্যকর (In Force)",
                passedDate = "৪ নভেম্বর, ১৯৭২",
                effectiveDate = "১৬ ডিসেম্বর, ১৯৭২",
                totalSections = 153,
                isPopular = true,
                isConstitution = true
            ),
            LawEntity(
                id = 2,
                actNumber = "Act No. XLV of 1860",
                actYear = 1860,
                titleBn = "দ্য পিনাল কোড, ১৮৬০ (দণ্ডবিধি)",
                titleEn = "The Penal Code, 1860",
                shortDescriptionBn = "বাংলাদেশের প্রধান ফৌজদারি আইন যা অপরাধসমূহের সংজ্ঞা এবং শাস্তির বিধান নির্ধারণ করে।",
                fullIntroductionBn = "১৮৬০ সালের ৬ অক্টোবর প্রণীত আইন নং ৪৫। এই আইন দ্বারা বাংলাদেশের ভূখণ্ডে সংঘটিত অপরাধসমূহের সুস্পষ্ট সংজ্ঞা, উপাদান, সাধারণ ব্যতিক্রম এবং সাজার পরিমাপ সংজ্ঞায়িত করা হয়েছে।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-11.html",
                status = "কার্যকর (In Force)",
                passedDate = "৬ অক্টোবর, ১৮৬০",
                effectiveDate = "১ জানুয়ারি, ১৮৬২",
                totalSections = 511,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 3,
                actNumber = "Act No. V of 1898",
                actYear = 1898,
                titleBn = "ফৌজদারি কার্যবিধি, ১৮৯৮",
                titleEn = "The Code of Criminal Procedure, 1898",
                shortDescriptionBn = "অপরাধ তদন্ত, বিনা পরোয়ানায় গ্রেপ্তার, রিমান্ড, চার্জশিট, বিচার ও জামিনের আইনি কার্যপদ্ধতি।",
                fullIntroductionBn = "বাংলাদেশের ফৌজদারি আদালতসমূহের গঠন, এখতিয়ার, পুলিশি তদন্ত, এফআইআর (FIR), অভিযোগ গঠন, বিচার, আপিল, রিভিশন এবং জামিন সংক্রান্ত বিধানাবলীর বিস্তারিত আইনি কাঠামো।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-75.html",
                status = "কার্যকর (In Force)",
                passedDate = "২২ মার্চ, ১৮৯৮",
                effectiveDate = "১ জুলাই, ১৮৯৮",
                totalSections = 565,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 4,
                actNumber = "Act No. I of 1872",
                actYear = 1872,
                titleBn = "সাক্ষ্য আইন, ১৮৭২",
                titleEn = "The Evidence Act, 1872",
                shortDescriptionBn = "আদালতে সাক্ষ্য গ্রহণ, প্রাসঙ্গিকতা, প্রমাণের দায়িত্ব এবং ডিজিটাল সাক্ষ্যের আধুনিক আইনি বিধান।",
                fullIntroductionBn = "বিচারিক কার্যক্রমে মৌখিক, দালিলিক ও ইলেকট্রনিক প্রমাণের গ্রহণযোগ্যতা, স্বীকারোক্তি, জেরা, জবানবন্দি ও প্রমাণের দায় সংক্রান্ত বিধিবিধান।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-24.html",
                status = "কার্যকর (In Force)",
                passedDate = "১৫ মার্চ, ১৮৭২",
                effectiveDate = "১ সেপ্টেম্বর, ১৮৭২",
                totalSections = 167,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 5,
                actNumber = "Act No. V of 1908",
                actYear = 1908,
                titleBn = "দেওয়ানি কার্যবিধি, ১৯০৮",
                titleEn = "The Code of Civil Procedure, 1908",
                shortDescriptionBn = "দেওয়ানি মামলা পরিচালনা, আরজি দাখিল, সমন, নিষেধাজ্ঞা, ডিক্রি জারি ও আপিল সংক্রান্ত বিধি।",
                fullIntroductionBn = "দেওয়ানি আদালতের এখতিয়ার, আরজি (Plaint), জবাব (Written Statement), মধ্যবর্তী আদেশ, অস্থায়ী নিষেধাজ্ঞা ও ডিক্রি বাস্তবায়নের আইনি কার্যপ্রণালী।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-86.html",
                status = "কার্যকর (In Force)",
                passedDate = "২১ মার্চ, ১৯০৮",
                effectiveDate = "১ জানুয়ারি, ১৯০৯",
                totalSections = 158,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 6,
                actNumber = "আইন নং ৮, ২০০০",
                actYear = 2000,
                titleBn = "নারী ও শিশু নির্যাতন দমন আইন, ২০০০",
                titleEn = "The Prevention of Women and Children Repression Act, 2000",
                shortDescriptionBn = "নারী ও শিশুদের বিরুদ্ধে সংঘটিত সহিংসতা, ধর্ষণ, যৌতুক ও যৌন নিপীড়নের কঠোর শাস্তির বিশেষ আইন।",
                fullIntroductionBn = "নারী ও শিশুদের সুরক্ষা নিশ্চিতকরণে বিশেষ ট্রাইব্যুনাল গঠন, দ্রুত বিচার এবং কঠোর সাজার বিধান সম্বলিত আইন (২০২০ সালের সংশোধনীসহ)।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-835.html",
                status = "কার্যকর (সংশোধিত ২০২০)",
                passedDate = "১৪ ফেব্রুয়ারি, ২০০০",
                effectiveDate = "১৪ ফেব্রুয়ারি, ২০০০",
                totalSections = 34,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 7,
                actNumber = "Act No. I of 1877",
                actYear = 1877,
                titleBn = "সুনির্দিষ্ট প্রতিকার আইন, ১৮৭৭",
                titleEn = "The Specific Relief Act, 1877",
                shortDescriptionBn = "সম্পত্তির দখল পুনরুদ্ধার, চুক্তি কার্যকর, দলিল বাতিল ও স্থায়ী নিষেধাজ্ঞার দেওয়ানি বিধান।",
                fullIntroductionBn = "বেআইনি বেদখল প্রতিকার (ধারা ৯), চুক্তি প্রতিপালন, স্বত্ব ঘোষণা (ধারা ৪২) এবং নিষেধাজ্ঞার মাধ্যমে সুনির্দিষ্ট দেওয়ানি অধিকার প্রতিষ্ঠার আইন।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-32.html",
                status = "কার্যকর (In Force)",
                passedDate = "৭ ফেব্রুয়ারি, ১৮৭৭",
                effectiveDate = "১ মে, ১৮৭৭",
                totalSections = 57,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 8,
                actNumber = "আইন নং ৬৩, ২০১৮",
                actYear = 2018,
                titleBn = "মাদকদ্রব্য নিয়ন্ত্রণ আইন, ২০১৮",
                titleEn = "The Narcotics Control Act, 2018",
                shortDescriptionBn = "মাদকদ্রব্য উৎপাদন, পাচার, বিক্রি ও সেবন রোধে কঠোর শাস্তির আইন।",
                fullIntroductionBn = "ইয়াবা, আইস, হেরোইনসহ ক্ষতিকর মাদকের শ্রেণিবিভাজন এবং সর্বোচ্চ মৃত্যুদণ্ডের সাজার বিধান সম্বলিত সমসাময়িক আইন।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-1271.html",
                status = "কার্যকর (In Force)",
                passedDate = "২৭ ডিসেম্বর, ২০১৮",
                effectiveDate = "২ জানুয়ারি, ২০১৯",
                totalSections = 74,
                isPopular = false,
                isConstitution = false
            ),
            LawEntity(
                id = 9,
                actNumber = "আইন নং ৫, ২০০৪",
                actYear = 2004,
                titleBn = "দুর্নীতি দমন কমিশন আইন, ২০০৪",
                titleEn = "The Anti-Corruption Commission Act, 2004",
                shortDescriptionBn = "দুর্নীতি প্রতিরোধ, সম্পদ বিবরণী তলব এবং জ্ঞাত আয়বহির্ভূত সম্পদ অর্জনের বিচার।",
                fullIntroductionBn = "দুর্নীতি দমন কমিশন (দুদক) প্রতিষ্ঠা, ক্ষমতা এবং দুর্নীতির অপরাধ তদন্ত ও প্রসিকিউশনের বিশেষ আইনি কাঠামো।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-910.html",
                status = "কার্যকর (In Force)",
                passedDate = "২৩ ফেব্রুয়ারি, ২০০৪",
                effectiveDate = "৯ মে, ২০০৪",
                totalSections = 38,
                isPopular = false,
                isConstitution = false
            ),
            LawEntity(
                id = 10,
                actNumber = "Act No. IX of 1908",
                actYear = 1908,
                titleBn = "তামাদি আইন, ১৯০৮",
                titleEn = "The Limitation Act, 1908",
                shortDescriptionBn = "আদালতে মোকদ্দমা, আপিল ও দরখাস্ত দাখিলের নির্ধারিত সময়সীমা ও বিলম্ব মার্জনা।",
                fullIntroductionBn = "নির্দিষ্ট সময়ের মধ্যে মামলা দায়ের না করিলে অধিকারের সমাপ্তি (ধারা ৩) এবং উপযুক্ত কারণে বিলম্ব মার্জনা (ধারা ৫) সংক্রান্ত আইনি বিধান।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-88.html",
                status = "কার্যকর (In Force)",
                passedDate = "৭ আগস্ট, ১৯০৮",
                effectiveDate = "১ জানুয়ারি, ১৯০৯",
                totalSections = 32,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 11,
                actNumber = "অধ্যাদেশ নং ৮, ১৯৬১",
                actYear = 1961,
                titleBn = "মুসলিম পারিবারিক আইন অধ্যাদেশ, ১৯৬১",
                titleEn = "The Muslim Family Laws Ordinance, 1961",
                shortDescriptionBn = "বিবাহ নিবন্ধন, বহুবিবাহের অনুমতি, তালাকের নোটিশ, উত্তরাধিকার ও খোরপোশের বিধান।",
                fullIntroductionBn = "মুসলিম পারিবারিক বিরোধ নিষ্পত্তিতে সালিসি পরিষদ, তালাকের কার্যকারিতা এবং পৌত্র-পৌত্রীর উত্তরাধিকার অধিকার রক্ষার মাইলফলক আইন।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-305.html",
                status = "কার্যকর (In Force)",
                passedDate = "২ মার্চ, ১৯৬১",
                effectiveDate = "১৫ জুলাই, ১৯৬১",
                totalSections = 13,
                isPopular = true,
                isConstitution = false
            ),
            LawEntity(
                id = 12,
                actNumber = "আইন নং ৪৭, ২০১৮",
                actYear = 2018,
                titleBn = "সড়ক পরিবহন আইন, ২০১৮",
                titleEn = "The Road Transport Act, 2018",
                shortDescriptionBn = "যানবাহন চালনা, ড্রাইভিং লাইসেন্স, মোটরযান নিবন্ধন ও মারাত্মক সড়ক দুর্ঘটনার শাস্তি।",
                fullIntroductionBn = "সড়ক নিরাপত্তা নিশ্চিতকরণ, বেপরোয়া যান চলাচলে প্রাণহানির বিচার এবং মোটরযান পরিচালনার বিস্তৃত বিধানাবলী।",
                sourceUrl = "https://bdlaws.minlaw.gov.bd/act-1262.html",
                status = "কার্যকর (In Force)",
                passedDate = "১১ অক্টোবর, ২০১৮",
                effectiveDate = "১ নভেম্বর, ২০১৯",
                totalSections = 126,
                isPopular = false,
                isConstitution = false
            )
        )
    }

    fun getInitialLawCategoryCrossRefs(): List<LawCategoryCrossRef> {
        return listOf(
            LawCategoryCrossRef(1, 1),  // Constitution -> Category 1
            LawCategoryCrossRef(2, 2),  // Penal Code -> Criminal Laws
            LawCategoryCrossRef(3, 2),  // CrPC -> Criminal Laws
            LawCategoryCrossRef(3, 3),  // CrPC -> CrPC
            LawCategoryCrossRef(4, 4),  // Evidence Act -> Evidence
            LawCategoryCrossRef(5, 5),  // CPC -> CPC
            LawCategoryCrossRef(6, 6),  // Women & Children -> Category 6
            LawCategoryCrossRef(6, 2),  // Women & Children -> Criminal Laws
            LawCategoryCrossRef(7, 5),  // Specific Relief Act -> Civil Laws
            LawCategoryCrossRef(7, 9),  // Specific Relief Act -> Land & Property
            LawCategoryCrossRef(8, 7),  // Narcotics -> Category 7
            LawCategoryCrossRef(8, 2),  // Narcotics -> Criminal Laws
            LawCategoryCrossRef(9, 12), // Anti-Corruption -> Category 12
            LawCategoryCrossRef(10, 5), // Limitation Act -> Civil Laws
            LawCategoryCrossRef(10, 20),// Limitation Act -> Special Laws
            LawCategoryCrossRef(11, 10),// Muslim Family Laws -> Family Laws
            LawCategoryCrossRef(12, 15) // Road Transport -> Category 15
        )
    }

    fun getInitialChapters(): List<ChapterEntity> {
        return listOf(
            // Constitution Chapters
            ChapterEntity(1, 1, "প্রস্তাবনা", "Preamble", "প্রস্তাবনা ও ঘোষণাপত্র", "Preamble", 1),
            ChapterEntity(2, 1, "প্রথম ভাগ", "Part I", "প্রজাতন্ত্র", "The Republic", 2),
            ChapterEntity(3, 1, "দ্বিতীয় ভাগ", "Part II", "রাষ্ট্র পরিচালনার মূলনীতি", "Fundamental Principles of State Policy", 3),
            ChapterEntity(4, 1, "তৃতীয় ভাগ", "Part III", "মৌলিক অধিকার", "Fundamental Rights", 4),
            ChapterEntity(5, 1, "ষষ্ঠ ভাগ", "Part VI", "বিচার বিভাগ ও সুপ্রিম কোর্ট", "The Judiciary", 5),

            // Penal Code Chapters
            ChapterEntity(6, 2, "অধ্যায় ১", "Chapter I", "প্রারম্ভিক", "Introduction", 1),
            ChapterEntity(7, 2, "অধ্যায় ২", "Chapter II", "সাধারণ ব্যাখ্যা", "General Explanations", 2),
            ChapterEntity(8, 2, "অধ্যায় ৩", "Chapter III", "দণ্ড সম্পর্কিত বিধান", "Of Punishments", 3),
            ChapterEntity(9, 2, "অধ্যায় ৪", "Chapter IV", "সাধারণ ব্যতিক্রম ও আত্মরক্ষা", "General Exceptions & Private Defence", 4),
            ChapterEntity(10, 2, "অধ্যায় ৮", "Chapter VIII", "জনশান্তি বিনষ্টকারী অপরাধ", "Of Offences Against the Public Tranquillity", 5),
            ChapterEntity(11, 2, "অধ্যায় ১৬", "Chapter XVI", "মানবদেহ বিরোধী অপরাধ (খুন, আঘাত)", "Of Offences Affecting the Human Body", 6),
            ChapterEntity(12, 2, "অধ্যায় ১৭", "Chapter XVII", "সম্পত্তি সংক্রান্ত অপরাধ (চুরি, প্রতারণা)", "Of Offences Against Property", 7),
            ChapterEntity(13, 2, "অধ্যায় ১৮", "Chapter XVIII", "দলিলপত্র ও জালিয়াতি সংক্রান্ত অপরাধ", "Of Offences Relating to Documents", 8),
            ChapterEntity(14, 2, "অধ্যায় ২১", "Chapter XXI", "মানহানি সংক্রান্ত অপরাধ", "Of Defamation", 9),

            // CrPC Chapters
            ChapterEntity(15, 3, "অধ্যায় ৫", "Chapter V", "গ্রেপ্তার, পলায়ন ও পুনঃগ্রেপ্তার", "Of Arrest, Escape and Retaking", 1),
            ChapterEntity(16, 3, "অধ্যায় ১১", "Chapter XI", "শান্তি রক্ষা ও শান্তি ভঙ্গের আশঙ্কা", "Of Maintenance of Public Order", 2),
            ChapterEntity(17, 3, "অধ্যায় ১৪", "Chapter XIV", "পুলিশের নিকট সংবাদ ও তদন্ত (এফআইআর)", "Information to Police and Their Powers to Investigate", 3),
            ChapterEntity(18, 3, "অধ্যায় ৩১", "Chapter XXXI", "আপিল ও রিভিশন", "Of Appeals", 4),
            ChapterEntity(19, 3, "অধ্যায় ৩৯", "Chapter XXXIX", "জামিন সংক্রান্ত বিধানাবলী", "Of Bail", 5),
            ChapterEntity(20, 3, "অধ্যায় ৪৬", "Chapter XLVI", "আদালতের সহজাত ক্ষমতা ও বিবিধ", "Miscellaneous & Inherent Powers", 6),

            // Evidence Act Chapters
            ChapterEntity(21, 4, "অধ্যায় ১", "Chapter I", "প্রারম্ভিক ও সংজ্ঞা", "Preliminary", 1),
            ChapterEntity(22, 4, "অধ্যায় ২", "Chapter II", "ঘটনার প্রাসঙ্গিকতা ও স্বীকারোক্তি", "Of the Relevancy of Facts", 2),
            ChapterEntity(23, 4, "অধ্যায় ৭", "Chapter VII", "প্রমাণের দায়ভার (Burden of Proof)", "Of the Burden of Proof", 3),
            ChapterEntity(24, 4, "অধ্যায় ১০", "Chapter X", "সাক্ষীদের পরীক্ষা ও জেরা (Cross-examination)", "Of the Examination of Witnesses", 4),

            // CPC Chapters
            ChapterEntity(25, 5, "অংশ ১", "Part I", "সাধারণভাবে মোকদ্দমা ও এখতিয়ার", "Suits in General & Jurisdiction", 1),
            ChapterEntity(26, 5, "অংশ ৭", "Part VII", "আপিল ও রিভিশন", "Appeals & Revision", 2),
            ChapterEntity(27, 5, "অংশ ১১", "Part XI", "আদালতের সহজাত ক্ষমতা (১৫১ ধারা)", "Miscellaneous & Inherent Powers", 3),

            // Specific Relief Chapters
            ChapterEntity(28, 7, "অধ্যায় ১", "Chapter I", "সম্পত্তির দখল পুনরুদ্ধার", "Of Recovering Possession of Property", 1),
            ChapterEntity(29, 7, "অধ্যায় ২", "Chapter II", "চুক্তির সুনির্দিষ্ট সম্পাদন", "Of the Specific Performance of Contracts", 2),
            ChapterEntity(30, 7, "অধ্যায় ৫", "Chapter V", "দলিল বাতিল", "Of the Cancellation of Instruments", 3),
            ChapterEntity(31, 7, "অধ্যায় ৬", "Chapter VI", "ঘোষণামূলক ডিক্রি", "Of Declaratory Decrees", 4),
            ChapterEntity(32, 7, "অধ্যায় ১০", "Chapter X", "চিরতরে ও অস্থায়ী নিষেধাজ্ঞা", "Of Injunctions Generally", 5),

            // Women & Children Repression Act Chapters
            ChapterEntity(33, 6, "মূল বিধান", "Main Provisions", "অপরাধ ও শাস্তির বিধান", "Offences and Penalties", 1),
            // Narcotics Control Act Chapters
            ChapterEntity(34, 8, "অধ্যায় ৬", "Chapter VI", "অপরাধ ও দণ্ড সারণি", "Offences and Penalties", 1),
            // Anti-Corruption Commission Act Chapters
            ChapterEntity(35, 9, "মূল বিধান", "Main Provisions", "কমিশন ও দুর্নীতি সংক্রান্ত বিধান", "Commission & Offences", 1),
            // Limitation Act Chapters
            ChapterEntity(36, 10, "অধ্যায় ২", "Chapter II", "মোকদ্দমা, আপিল ও দরখাস্তের তামাদি", "Limitation of Suits, Appeals and Applications", 1),
            // Muslim Family Laws Ordinance Chapters
            ChapterEntity(37, 11, "মূল ধারা", "Main Sections", "বিবাহ, বহুবিবাহ ও তালাক বিধান", "Marriage, Polygamy & Talaq", 1),
            // Road Transport Act Chapters
            ChapterEntity(38, 12, "মূল বিধান", "Main Provisions", "সড়ক নিরাপত্তা ও দণ্ডবিধান", "Road Safety & Penalties", 1)
        )
    }

    fun getInitialSections(): List<SectionEntity> {
        val lawSourceMap = getInitialLaws().associate { it.id to it.sourceUrl }
        val rawSections = listOf(
            // ============================================
            // 1. CONSTITUTION (গণপ্রজাতন্ত্রী বাংলাদেশের সংবিধান)
            // ============================================
            SectionEntity(
                id = 101,
                lawId = 1,
                chapterId = 2,
                sectionNumberBn = "৭",
                sectionNumberEn = "7",
                titleBn = "সংবিধানের প্রাধান্য",
                titleEn = "Supremacy of the Constitution",
                fullContentBn = """(১) প্রজাতন্ত্রের সকল ক্ষমতার মালিক জনগণ; এবং জনগণের পক্ষে সেই ক্ষমতার প্রয়োগ কেবল এই সংবিধানের অধীন ও কর্তৃত্বে কার্যকর হইবে।
(২) জনগণের অভিপ্রায়ের পরম অভিব্যক্তিরূপে এই সংবিধান প্রজাতন্ত্রের সর্বোচ্চ আইন এবং অন্য কোন আইন যদি এই সংবিধানের সহিত অসামঞ্জস্য হয়, তাহা হইলে সেই আইনের যতখানি অসামঞ্জস্যপূর্ণ, ততখানি বাতিল হইবে।""",
                fullContentEn = "(1) All powers in the Republic belong to the people, and their exercise on behalf of the people shall be effected only under, and by the authority of, this Constitution. (2) This Constitution is, as the solemn expression of the will of the people, the supreme law of the Republic, and if any other law is inconsistent with this Constitution that other law shall, to the extent of the inconsistency, be void.",
                simplifiedExplanationBn = "এই অনুচ্ছেদটি সংবিধানকে বাংলাদেশের সর্বোচ্চ সার্বভৌম আইন ঘোষণা করেছে। সংসদ প্রণীত যেকোনো সাধারণ আইন যদি সংবিধানের সঙ্গে সাংঘর্ষিক হয়, সুপ্রিম কোর্ট বিচারিক পর্যালোচনার (Judicial Review) মাধ্যমে তা বাতিল ঘোষণা করতে পারে। রাষ্ট্রের সকল কর্তৃপক্ষ সংবিধানের অধীন।",
                userNotes = null,
                punishmentSummaryBn = "সংবিধান পরিপন্থী যেকোনো আইন বাতিল বলে গণ্য হবে",
                bailableStatus = "সাংবিধানিক বিধান",
                cognizableStatus = "সার্বভৌম এখতিয়ার",
                trialCourt = "বাংলাদেশ সুপ্রিম কোর্ট",
                orderIndex = 1
            ),
            SectionEntity(
                id = 102,
                lawId = 1,
                chapterId = 4,
                sectionNumberBn = "২৭",
                sectionNumberEn = "27",
                titleBn = "আইনের দৃষ্টিতে সমতা",
                titleEn = "Equality before law",
                fullContentBn = "সকল নাগরিক আইনের দৃষ্টিতে সমান এবং আইনের সমান আশ্রয় লাভের অধিকারী।",
                fullContentEn = "All citizens are equal before law and are entitled to equal protection of law.",
                simplifiedExplanationBn = "রাষ্ট্রের যেকোনো নাগরিক, তিনি ধনী-দরিদ্র, ক্ষমতাবান বা সাধারণ মানুষ যাই হোন না কেন, আইনের চোখে সবাই এক। রাষ্ট্র কোনো নাগরিকের প্রতি অযৌক্তিক বৈষম্য করতে পারবে না এবং সবাইকে সমভাবে আইনি নিরাপত্তা দিতে বাধ্য।",
                userNotes = null,
                punishmentSummaryBn = "মৌলিক অধিকার সুরক্ষা",
                bailableStatus = "মৌলিক অধিকার",
                cognizableStatus = "রিটযোগ্য",
                trialCourt = "হাইকোর্ট বিভাগ (সুপ্রিম কোর্ট)",
                orderIndex = 2
            ),
            SectionEntity(
                id = 103,
                lawId = 1,
                chapterId = 4,
                sectionNumberBn = "৩১",
                sectionNumberEn = "31",
                titleBn = "আইনের আশ্রয়লাভের অধিকার",
                titleEn = "Right to protection of law",
                fullContentBn = "আইনের আশ্রয়লাভ এবং আইনানুযায়ী ও কেবল আইনানুযায়ী ব্যবহারলাভ যেকোনো স্থানে অবস্থানরত প্রত্যেক নাগরিকের এবং সাময়িকভাবে বাংলাদেশে অবস্থানরত অপরাপর ব্যক্তির অবিচ্ছেদ্য অধিকার এবং বিশেষতঃ আইন ব্যতীত এমন কোনো ব্যবস্থা গ্রহণ করা যাইবে না, যাহাতে কোনো ব্যক্তির জীবন, স্বাধীনতা, দেহ, সুনাম বা সম্পত্তির হানি ঘটে।",
                fullContentEn = "To enjoy the protection of the law, and to be treated in accordance with law, and only in accordance with law, is the inalienable right of every citizen, wherever he may be, and of every other person for the time being within Bangladesh, and in particular no action detrimental to the life, liberty, body, reputation or property of any person shall be taken except in accordance with law.",
                simplifiedExplanationBn = "এটি বাংলাদেশের নাগরিকদের জন্য অত্যন্ত প্রভাবশালী একটি সাংবিধানিক রক্ষাকবচ (Due Process of Law)। আইনি বৈধতা ও সুস্পষ্ট পদ্ধতি ছাড়া পুলিশ বা প্রশাসন কোনো ব্যক্তির জীবন, ব্যক্তি-স্বাধীনতা, শারীরিক ক্ষতি, সম্মানহানি বা সম্পত্তি বাজেয়াপ্ত করতে পারে না।",
                userNotes = null,
                punishmentSummaryBn = "আইনবহির্ভূত পদক্ষেপ সম্পূর্ণ অবৈধ",
                bailableStatus = "মৌলিক অধিকার",
                cognizableStatus = "রিটযোগ্য",
                trialCourt = "হাইকোর্ট বিভাগ (সুপ্রিম কোর্ট)",
                orderIndex = 3
            ),
            SectionEntity(
                id = 104,
                lawId = 1,
                chapterId = 4,
                sectionNumberBn = "৩২",
                sectionNumberEn = "32",
                titleBn = "জীবন ও ব্যক্তি-স্বাধীনতার অধিকার রক্ষণ",
                titleEn = "Protection of right to life and personal liberty",
                fullContentBn = "আইনানুযায়ী ব্যতীত কোনো ব্যক্তিকে জীবন বা ব্যক্তি-স্বাধীনতা হইতে বঞ্চিত করা যাইবে না।",
                fullContentEn = "No person shall be deprived of life or personal liberty save in accordance with law.",
                simplifiedExplanationBn = "আইনের সুনির্দিষ্ট বিধান ও বৈধ আদালতের নির্দেশ ব্যতীত কোনো নাগরিকের জীবনাবসান ঘটানো বা তাকে গ্রেপ্তার/আটক রেখে স্বাধীনতা হরণ করা যাবে না। বিচারবহির্ভূত হত্যাকাণ্ড ও বেআইনি আটকের বিরুদ্ধে এটি সর্বোচ্চ অধিকার।",
                userNotes = null,
                punishmentSummaryBn = "জীবন ও ব্যক্তিস্বাধীনতার মৌলিক সুরক্ষা",
                bailableStatus = "মৌলিক অধিকার",
                cognizableStatus = "রিটযোগ্য",
                trialCourt = "হাইকোর্ট বিভাগ (সুপ্রিম কোর্ট)",
                orderIndex = 4
            ),
            SectionEntity(
                id = 105,
                lawId = 1,
                chapterId = 5,
                sectionNumberBn = "১০২",
                sectionNumberEn = "102",
                titleBn = "কতিপয় আদেশ ও নির্দেশ প্রভৃতি দানের ক্ষেত্রে হাইকোর্ট বিভাগের ক্ষমতা (রিট এখতিয়ার)",
                titleEn = "Powers of High Court Division to issue certain orders and directions, etc.",
                fullContentBn = """(১) কোনো সংক্ষুব্ধ ব্যক্তির আবেদনক্রমে এই সংবিধানের তৃতীয় ভাগের দ্বারা নিশ্চিতকৃত মৌলিক অধিকারসমূহের যেকোনো একটি বলবৎ করিবার জন্য প্রজাতন্ত্রের বিষয়াবলীর সহিত সম্পর্কিত কোনো দায়িত্ব পালনকারী ব্যক্তিসহ যেকোনো ব্যক্তি বা কর্তৃপক্ষকে হাইকোর্ট বিভাগ উপযুক্ত নির্দেশাবলী বা আদেশাবলী দান করিতে পারিবেন।
(২) হাইকোর্ট বিভাগের সন্তোষ অনুযায়ী যদি কোনো সমফলপ্রদ বিকল্প প্রতিকারের ব্যবস্থা না থাকে, তাহা হইলে-
(ক) কোনো সংক্ষুব্ধ ব্যক্তির আবেদনক্রমে নির্দেশ দেওয়া যে প্রজাতন্ত্র বা স্থানীয় কর্তৃপক্ষের দায়িত্বে থাকা কোনো ব্যক্তি আইনের কর্তৃত্ব ছাড়া কোনো কাজ করিবেন না বা আইনানুগ দায়িত্ব পালন করবেন;
(খ) বেআইনিভাবে আটক কোনো ব্যক্তিকে আদালতের সামনে হাজির করার নির্দেশ (Habeas Corpus);
(গ) কোনো ব্যক্তি কোন কর্তৃত্ববলে কোনো পদে অধিষ্ঠিত রয়েছেন তা প্রদর্শনের নির্দেশ (Quo-Warranto)।""",
                fullContentEn = "Power of the High Court Division to enforce fundamental rights and issue writ directions (Mandamus, Certiorari, Prohibition, Habeas Corpus, Quo-Warranto).",
                simplifiedExplanationBn = "এটি বহুল চর্চিত রিট আবেদন (Writ Petition)-এর মূল সাংবিধানিক উৎস। নাগরিকের কোনো মৌলিক অধিকার খর্ব হলে অথবা সরকারি কর্তৃপক্ষ কোনো বেআইনি পদক্ষেপ গ্রহণ করলে বা আইনসম্মত দায়িত্ব পালনে ব্যর্থ হলে, হাইকোর্ট বিভাগে ১০২ অনুচ্ছেদে সরাসরি রিট দায়ের করে প্রতিকার পাওয়া যায়।",
                userNotes = null,
                punishmentSummaryBn = "হাইকোর্ট বিভাগ যেকোনো বেআইনি আদেশ স্থগিত বা বাতিল করতে পারে",
                bailableStatus = "সাংবিধানিক রিট এখতিয়ার",
                cognizableStatus = "সরাসরি সুপ্রিম কোর্টে আবেদন",
                trialCourt = "হাইকোর্ট বিভাগ, বাংলাদেশ সুপ্রিম কোর্ট",
                orderIndex = 5
            ),

            // ============================================
            // 2. THE PENAL CODE, 1860 (দণ্ডবিধি, ১৮৬০)
            // ============================================
            SectionEntity(
                id = 201,
                lawId = 2,
                chapterId = 7,
                sectionNumberBn = "৩৪",
                sectionNumberEn = "34",
                titleBn = "সাধারণ অভিপ্রায় বাস্তবায়নে একাধিক ব্যক্তি কর্তৃক কৃত কার্য",
                titleEn = "Acts done by several persons in furtherance of common intention",
                fullContentBn = "যখন একাধিক ব্যক্তি কর্তৃক তাহাদের সকলের সাধারণ অভিপ্রায় বাস্তবায়নের উদ্দেশ্যে কোনো অপরাধমূলক কার্য সম্পাদন করা হয়, তখন তাহাদের প্রত্যেকের বিরুদ্ধে এমনভাবে ব্যবস্থা গ্রহণ করা হইবে যেন সেই ব্যক্তি একাই তাহা সম্পন্ন করিয়াছে।",
                fullContentEn = "When a criminal act is done by several persons in furtherance of the common intention of all, each of such persons is liable for that act in the same manner as if it were done by him alone.",
                simplifiedExplanationBn = "যৌথ অপরাধের ক্ষেত্রে এটি একটি অত্যন্ত গুরুত্বপূর্ণ ধারা (Doctrine of Joint Liability)। যদি দুই বা ততোধিক ব্যক্তি পূর্বপরিকল্পনা ও সাধারণ অভিপ্রায় নিয়ে কোনো অপরাধে অংশ নেয়, তাহলে অপরাধ সংগঠনের সময় প্রত্যেকে সরাসরি হাত দিয়ে আঘাত না করলেও প্রত্যেকে মূল অপরাধীর সমতুল্য সাজা পাবে।",
                userNotes = null,
                punishmentSummaryBn = "মূল অপরাধের সমপরিমাণ শাস্তি",
                bailableStatus = "মূল ধারার প্রকৃতির ওপর নির্ভরশীল",
                cognizableStatus = "মূল ধারার প্রকৃতির ওপর নির্ভরশীল",
                trialCourt = "সংশ্লিষ্ট বিচারিক আদালত",
                orderIndex = 1
            ),
            SectionEntity(
                id = 202,
                lawId = 2,
                chapterId = 9,
                sectionNumberBn = "৯৬",
                sectionNumberEn = "96",
                titleBn = "ব্যক্তিগত প্রতিরক্ষামূলক কার্য",
                titleEn = "Things done in private defence",
                fullContentBn = "ব্যক্তিগত আত্মরক্ষার অধিকার প্রয়োগকালে যে কার্য করা হয়, তাহা কোনো অপরাধ নহে।",
                fullContentEn = "Nothing is an offence which is done in the exercise of the right of private defence.",
                simplifiedExplanationBn = "আইন প্রত্যেক ব্যক্তিকে নিজের দেহ, জীবন ও সম্পত্তি রক্ষায় বলপ্রয়োগের আত্মরক্ষামূলক অধিকার দিয়েছে। বিপদের মুখে নিজেকে রক্ষা করতে গিয়ে সংঘটিত কার্যক্রম অপরাধ হিসেবে গণ্য হবে না, তবে তা অবশ্যই আইনের ৯৯ ধারায় উল্লিখিত সীমার মধ্যে থাকতে হবে।",
                userNotes = null,
                punishmentSummaryBn = "কোনো অপরাধ নহে (আইনগত দায়মুক্তি)",
                bailableStatus = "আইনগত প্রতিরক্ষা",
                cognizableStatus = "তদন্তে প্রমাণের বিষয়",
                trialCourt = "বিচারিক আদালত",
                orderIndex = 2
            ),
            SectionEntity(
                id = 203,
                lawId = 2,
                chapterId = 9,
                sectionNumberBn = "১০০",
                sectionNumberEn = "100",
                titleBn = "দেহের ব্যক্তিগত প্রতিরক্ষার অধিকার কখন মৃত্যু ঘটানো পর্যন্ত বিস্তৃত হয়",
                titleEn = "When the right of private defence of the body extends to causing death",
                fullContentBn = """দেহের ব্যক্তিগত প্রতিরক্ষার অধিকারের ক্ষেত্র নিম্নে বর্ণিত ছয়টি ক্ষেত্রে আক্রমণকারীর মৃত্যু ঘটানো বা অন্য কোনো ক্ষতিসাধন পর্যন্ত বিস্তৃত হইতে পারে:
১. এমন আক্রমণ যাহা যুক্তিসঙ্গতভাবে মৃত্যুর আশঙ্কা সৃষ্টি করে;
২. এমন আক্রমণ যাহা যুক্তিসঙ্গতভাবে গুরুতর আঘাতের আশঙ্কা সৃষ্টি করে;
৩. ধর্ষণের উদ্দেশ্যে কৃত আক্রমণ;
৪. অস্বাভাবিক লালসা চরিতার্থ করিবার উদ্দেশ্যে কৃত আক্রমণ;
৫. অপহরণ বা অপবাহনের উদ্দেশ্যে কৃত আক্রমণ;
৬. কোনো ব্যক্তিকে বেআইনিভাবে আটক করিবার উদ্দেশ্যে আক্রমণ, যেখানে সে যুক্তিসঙ্গতভাবে মনে করে যে মুক্তির জন্য সে সরকারি কর্তৃপক্ষের সাহায্য পাইবে না।""",
                fullContentEn = "The right of private defence of the body extends to the voluntary causing of death if the offence which occasions the exercise of the right be of any of the six descriptions enumerated in this section.",
                simplifiedExplanationBn = "প্রাণঘাতী বিপদ, ধর্ষণ, বা মারাত্মক অপহরণের মুখোমুখি হলে আক্রমণকারীর মৃত্যু ঘটিয়ে হলেও নিজেকে বাঁচানো আইনত বৈধ। তবে আক্রমণ প্রতিহত করার যতটুকু বল প্রয়োজন, তার অতিরিক্ত বল প্রয়োগ করা যাবে না।",
                userNotes = null,
                punishmentSummaryBn = "শর্তসাপেক্ষে মৃত্যু ঘটানোর দায় থেকে সম্পূর্ণ মুক্তি",
                bailableStatus = "আইনি প্রতিরক্ষা",
                cognizableStatus = "বিচার্য বিষয়",
                trialCourt = "দায়রা আদালত",
                orderIndex = 3
            ),
            SectionEntity(
                id = 204,
                lawId = 2,
                chapterId = 11,
                sectionNumberBn = "৩০০",
                sectionNumberEn = "300",
                titleBn = "খুন (Murder)-এর সংজ্ঞা ও সাধারণ ব্যতিক্রম",
                titleEn = "Murder",
                fullContentBn = """ব্যতিক্রমসমূহ ব্যতীত সকল অপরাধজনক প্রাণনাশই খুন বলিয়া গণ্য হইবে, যদি কাজটি নিম্নরূপ হয়:
১. কাজটি মৃত্যু ঘটানোর নিশ্চিত অভিপ্রায়ে সম্পন্ন করা হয়; অথবা
২. অপরাধী এমন শারীরিক জখম ঘটানোর অভিপ্রায়ে করে যাহা অপরাধী জানে নিশ্চিত মৃত্যু ঘটাবে; অথবা
৩. সাধারণ অবস্থায় মৃত্যু ঘটাতে সমর্থ জখমের উদ্দেশ্যে করা হয়; অথবা
৪. এমন বিপজ্জনক কাজ করা হয় যাহার ফলে অবধারিত মৃত্যু ঘটিবে জেনেও কোনো যুক্তিযুক্ত অজুহাত ছাড়া কাজটি করা হয়।
ব্যতিক্রমসমূহ: তীব্র ও আকস্মিক প্ররোচনা (Grave and sudden provocation), আত্মরক্ষার অধিকারের মাত্রাতিরিক্ত প্রয়োগ, সরকারি কর্মচারীর আইনানুগ ক্ষমতার মাত্রাতিরিক্ত প্রয়োগ, কোনো পূর্বপরিকল্পনা ছাড়া আকস্মিক মারামারি।""",
                fullContentEn = "Definition of murder and five exceptions where culpable homicide is not murder.",
                simplifiedExplanationBn = "খুন ও অপরাধজনক প্রাণনাশের (Culpable Homicide) মধ্যকার পার্থক্য নির্দেশক মূল ধারা। পূর্বপরিকল্পনা ও স্পষ্ট হত্যার মানসিকতা থাকলে তা ৩০০ ধারার অধীনে খুন। আর ৫টি ব্যতিক্রমের কোনো একটিতে পড়লে তা খুনের পর্যায়ভুক্ত নয় এমন প্রাণনাশ (ধারা ৩০৪) হিসেবে গণ্য হয়।",
                userNotes = null,
                punishmentSummaryBn = "খুন প্রমাণিত হলে ধারা ৩০২ অনুযায়ী সর্বোচ্চ শাস্তি",
                bailableStatus = "অ-জামিনযোগ্য",
                cognizableStatus = "আমলযোগ্য",
                trialCourt = "দায়রা আদালত",
                orderIndex = 4
            ),
            SectionEntity(
                id = 205,
                lawId = 2,
                chapterId = 11,
                sectionNumberBn = "৩০২",
                sectionNumberEn = "302",
                titleBn = "খুনের শাস্তি (Punishment for murder)",
                titleEn = "Punishment for murder",
                fullContentBn = "যে ব্যক্তি খুন করে, সেই ব্যক্তি মৃত্যুদণ্ডে অথবা যাবজ্জীবন কারাদণ্ডে দণ্ডিত হইবে এবং তদুপরি অর্থদণ্ডেও দণ্ডিত হইবে।",
                fullContentEn = "Whoever commits murder shall be punished with death, or imprisonment for life, and shall also be liable to fine.",
                simplifiedExplanationBn = "দণ্ডবিধির সর্বাধিক ব্যবহৃত ও গুরুত্বপূর্ণ ধারা। খুনের অপরাধ সন্দেহাতীতভাবে প্রমাণিত হলে আদালতের কাছে কেবল দুটি সাজার সুযোগ থাকে: ১) মৃত্যুদণ্ড অথবা ২) যাবজ্জীবন কারাদণ্ড এবং সাথে অর্থদণ্ড বাধ্যতামূলক। এটি আমলযোগ্য, অ-জামিনযোগ্য এবং কোনোক্রমেই আপসযোগ্য নয়।",
                userNotes = null,
                punishmentSummaryBn = "মৃত্যুদণ্ড অথবা যাবজ্জীবন সশ্রম কারাদণ্ড এবং জরিমানা",
                bailableStatus = "অ-জামিনযোগ্য (Non-Bailable)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "দায়রা জজ আদালত (Court of Session)",
                orderIndex = 5
            ),
            SectionEntity(
                id = 206,
                lawId = 2,
                chapterId = 11,
                sectionNumberBn = "৩০৪ক",
                sectionNumberEn = "304A",
                titleBn = "বেপরোয়া বা অবহেলাজনিত কাজের ফলে মৃত্যু ঘটানো",
                titleEn = "Causing death by rash or negligent act",
                fullContentBn = "যে ব্যক্তি এমন কোনো বেপরোয়া বা অবহেলামূলক কাজের দ্বারা কোনো ব্যক্তির মৃত্যু ঘটায়, যাহা অপরাধজনক নরহত্যার পর্যায়ভুক্ত নহে, সেই ব্যক্তি অনধিক পাঁচ বৎসর পর্যন্ত যেকোনো মেয়াদের কারাদণ্ডে, অথবা অর্থদণ্ডে, অথবা উভয়বিধ দণ্ডেই দণ্ডিত হইবে।",
                fullContentEn = "Whoever causes the death of any person by doing any rash or negligent act not amounting to culpable homicide shall be punished with imprisonment of either description for a term which may extend to five years, or with fine, or with both.",
                simplifiedExplanationBn = "যেখানে হত্যার কোনো পূর্বপরিকল্পনা বা অভিপ্রায় ছিল না, কিন্তু চরম অসাবধানতা, বেপরোয়া যান চালনা বা দায়িত্বহীন অবহেলার কারণে কারো মৃত্যু ঘটে, সেখানে ৩০৪ক ধারা প্রযোজ্য।",
                userNotes = null,
                punishmentSummaryBn = "অনধিক ৫ বছরের কারাদণ্ড অথবা অর্থদণ্ড বা উভয় দণ্ড",
                bailableStatus = "জামিনযোগ্য (Bailable)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "প্রথম শ্রেণির ম্যাজিস্ট্রেট আদালত",
                orderIndex = 6
            ),
            SectionEntity(
                id = 207,
                lawId = 2,
                chapterId = 11,
                sectionNumberBn = "৩২৬",
                sectionNumberEn = "326",
                titleBn = "মারাত্মক অস্ত্র বা মাধ্যমে স্বেচ্ছাকৃতভাবে গুরুতর জখম করা",
                titleEn = "Voluntarily causing grievous hurt by dangerous weapons or means",
                fullContentBn = "যে ব্যক্তি গুলিবর্ষণ, ছুরি, ছোরা, অস্ত্র বা বিষাক্ত ও দাহ্য পদার্থ বা যেকোনো মারাত্মক অস্ত্রের মাধ্যমে স্বেচ্ছাকৃতভাবে মারাত্মক ও গুরুতর জখম সাধন করে, সেই ব্যক্তি যাবজ্জীবন কারাদণ্ডে অথবা অনধিক দশ বৎসর কারাদণ্ডে এবং অর্থদণ্ডেও দণ্ডিত হইবে।",
                fullContentEn = "Whoever voluntarily causes grievous hurt by means of any instrument for shooting, stabbing or cutting, shall be punished with imprisonment for life, or with imprisonment of either description for a term which may extend to ten years, and shall also be liable to fine.",
                simplifiedExplanationBn = "মারামারির মামলায় এটি অন্যতম নিয়মিত অভিযোগ। ধারালো অস্ত্র দিয়ে কুপিয়ে বা আঘাত করে হাড় ভাঙা, স্থায়ী অঙ্গহানি, দৃষ্টিশক্তি বা শ্রবণশক্তি নষ্ট করা ইত্যাদির জন্য এই কঠোর ধারা প্রয়োগ করা হয়।",
                userNotes = null,
                punishmentSummaryBn = "যাবজ্জীবন কারাদণ্ড অথবা ১০ বছর পর্যন্ত কারাদণ্ড এবং জরিমানা",
                bailableStatus = "অ-জামিনযোগ্য (Non-Bailable)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "দায়রা আদালত বা চিফ জুডিশিয়াল ম্যাজিস্ট্রেট",
                orderIndex = 7
            ),
            SectionEntity(
                id = 208,
                lawId = 2,
                chapterId = 12,
                sectionNumberBn = "৩৭৯",
                sectionNumberEn = "379",
                titleBn = "চুরির শাস্তি (Punishment for theft)",
                titleEn = "Punishment for theft",
                fullContentBn = "যে ব্যক্তি চুরি করে, সেই ব্যক্তি অনধিক তিন বৎসর পর্যন্ত যেকোনো মেয়াদের সশ্রম বা বিনাশ্রম কারাদণ্ডে, অথবা অর্থদণ্ডে, অথবা উভয়বিধ দণ্ডেই দণ্ডিত হইবে।",
                fullContentEn = "Whoever commits theft shall be punished with imprisonment of either description for a term which may extend to three years, or with fine, or with both.",
                simplifiedExplanationBn = "কারো সম্মতি ছাড়া অসদুপায়ে তার দখলভুক্ত কোনো স্থাবর/অস্থাবর সম্পত্তি সরিয়ে নেওয়াকে চুরি বলে (৩৭৮ ধারা)। ৩৭৯ ধারায় সাধারণ চুরির বিচার ও সর্বোচ্চ ৩ বছরের কারাদণ্ডের বিধান আছে। বাসগৃহের চুরির জন্য ৩৮০ ধারায় ৭ বছর পর্যন্ত শাস্তি হতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "অনধিক ৩ বছর কারাদণ্ড অথবা অর্থদণ্ড বা উভয় দণ্ড",
                bailableStatus = "অ-জামিনযোগ্য (তবে আদালত বিবেচনায় জামিন মঞ্জুর করতে পারে)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "যেকোনো ম্যাজিস্ট্রেট আদালত",
                orderIndex = 8
            ),
            SectionEntity(
                id = 209,
                lawId = 2,
                chapterId = 12,
                sectionNumberBn = "৩৯২",
                sectionNumberEn = "392",
                titleBn = "দস্যুতার শাস্তি (Punishment for robbery)",
                titleEn = "Punishment for robbery",
                fullContentBn = "যে ব্যক্তি দস্যুতা করে, সেই ব্যক্তি অনধিক দশ বৎসর পর্যন্ত সশ্রম কারাদণ্ডে দণ্ডিত হইবে এবং তদুপরি অর্থদণ্ডেও দণ্ডিত হইবে; এবং যদি দস্যুতার কাজটি সূর্যাস্ত ও সূর্যোদয়ের মধ্যবর্তী সময়ে রাজপথে সম্পন্ন হয়, তাহা হইলে কারাদণ্ডের মেয়াদ অনধিক চৌদ্দ বৎসর পর্যন্ত বৃদ্ধি পাইতে পারিবে।",
                fullContentEn = "Whoever commits robbery shall be punished with rigorous imprisonment for a term which may extend to ten years, and shall also be liable to fine; and, if the robbery be committed on the highway between sunset and sunrise, the imprisonment may be extended to fourteen years.",
                simplifiedExplanationBn = "ভয়ভীতি দেখিয়ে বা তাৎক্ষণিক মৃত্যুর বা আঘাতের আশঙ্কা সৃষ্টি করে জোরপূর্বক সম্পত্তি ছিনতাই করলে তা দস্যুতা (Robbery) হিসেবে গণ্য হয়। রাতে মহাসড়কে ছিনতাই হলে সাজার পরিমাণ বেড়ে ১৪ বছর পর্যন্ত হতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "অনধিক ১০ বছর (মহাসড়কে রাতে ১৪ বছর) সশ্রম কারাদণ্ড ও জরিমানা",
                bailableStatus = "অ-জামিনযোগ্য (Non-Bailable)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "দায়রা আদালত বা প্রথম শ্রেণির ম্যাজিস্ট্রেট",
                orderIndex = 9
            ),
            SectionEntity(
                id = 210,
                lawId = 2,
                chapterId = 12,
                sectionNumberBn = "৪০৬",
                sectionNumberEn = "406",
                titleBn = "অপরাধমূলক বিশ্বাসভঙ্গের শাস্তি",
                titleEn = "Punishment for criminal breach of trust",
                fullContentBn = "যে ব্যক্তি অপরাধমূলক বিশ্বাসভঙ্গ করে, সেই ব্যক্তি অনধিক তিন বৎসর পর্যন্ত যেকোনো মেয়াদের সশ্রম বা বিনাশ্রম কারাদণ্ডে, অথবা অর্থদণ্ডে, অথবা উভয়বিধ দণ্ডেই দণ্ডিত হইবে।",
                fullContentEn = "Whoever commits criminal breach of trust shall be punished with imprisonment of either description for a term which may extend to three years, or with fine, or with both.",
                simplifiedExplanationBn = "কোনো ব্যক্তির কাছে বিশ্বস্তভাবে কোনো টাকা বা সম্পত্তি আমানত হিসেবে রাখা হলে, সে যদি তা অসদুপায়ে আত্মসাৎ করে বা নিজের কাজে খরচ করে ফেলে, তবে তা ৪০৬ ধারায় অপরাধ। মামলায় প্রায়ই ৪০৬ ও ৪২০ ধারা একসাথে যুক্ত করা হয়।",
                userNotes = null,
                punishmentSummaryBn = "অনধিক ৩ বছর কারাদণ্ড অথবা অর্থদণ্ড বা উভয় দণ্ড",
                bailableStatus = "অ-জামিনযোগ্য (তবে আদালত বিবেচনায় জামিনযোগ্য)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "প্রথম শ্রেণির ম্যাজিস্ট্রেট",
                orderIndex = 10
            ),
            SectionEntity(
                id = 211,
                lawId = 2,
                chapterId = 12,
                sectionNumberBn = "৪২০",
                sectionNumberEn = "420",
                titleBn = "প্রতারণা ও অসাধুভাবে সম্পত্তি অর্পণে প্ররোচিত করা",
                titleEn = "Cheating and dishonestly inducing delivery of property",
                fullContentBn = "যে ব্যক্তি প্রতারণা করে এবং প্রতারিত ব্যক্তিকে কোনো সম্পত্তি হস্তান্তর করিতে বা মূল্যবান জামানত বা কোনো স্বাক্ষরিত বা সীলমোহরকৃত দলিল তৈয়ার, পরিবর্তন বা ধ্বংস করিতে অসাধুভাবে প্ররোচিত করে, সেই ব্যক্তি অনধিক সাত বৎসর পর্যন্ত যেকোনো মেয়াদের সশ্রম বা বিনাশ্রম কারাদণ্ডে দণ্ডিত হইবে এবং তদুপরি অর্থদণ্ডেও দণ্ডিত হইবে।",
                fullContentEn = "Whoever cheats and thereby dishonestly induces the person deceived to deliver any property, shall be punished with imprisonment of either description for a term which may extend to seven years, and shall also be liable to fine.",
                simplifiedExplanationBn = "বাংলাদেশের আদালতে সবচেয়ে পরিচিত ফৌজদারি ধারাগুলোর একটি। মিথ্যা আশ্বাস, ভুয়া ব্যবসা, জমি জালিয়াতি বা ভুয়া দলিলের মাধ্যমে কারো কাছ থেকে টাকা বা সম্পত্তি হাতিয়ে নিলে ৪২০ ধারা প্রযোজ্য। প্রাথমিক উপাদান হলো: লেনদেনের শুরু থেকেই অসৎ উদ্দেশ্য থাকা।",
                userNotes = null,
                punishmentSummaryBn = "অনধিক ৭ বছর সশ্রম বা বিনাশ্রম কারাদণ্ড এবং জরিমানা",
                bailableStatus = "অ-জামিনযোগ্য (Non-Bailable)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "প্রথম শ্রেণির ম্যাজিস্ট্রেট আদালত",
                orderIndex = 11
            ),
            SectionEntity(
                id = 212,
                lawId = 2,
                chapterId = 14,
                sectionNumberBn = "৪৯৯",
                sectionNumberEn = "499",
                titleBn = "মানহানির সংজ্ঞা ও ১০টি ব্যতিক্রম",
                titleEn = "Defamation",
                fullContentBn = """যে ব্যক্তি মৌখিক বা লিখিতভাবে বা অঙ্গভঙ্গি বা প্রকাশ্য চিহ্নাদির মাধ্যমে কোনো ব্যক্তির খ্যাতি নষ্ট করিবার উদ্দেশ্যে বা এমন আশঙ্কা থাকিয়াও কোনো ব্যক্তির সম্পর্কে কোনো অপবাদ প্রচার করে, সে সেই ব্যক্তির মানহানি করে বলিয়া গণ্য হইবে।
ব্যতিক্রমসমূহ: জনস্বার্থে সত্য প্রকাশ, সরকারি কর্মচারীর কাজের সমালোচনা, আদালতের কার্যক্রমের সত্য প্রতিবেদন, জনসাধারণের সুবিধার জন্য আদালতের রায়ের যথার্থ আলোচনা ইত্যাদি।""",
                fullContentEn = "Defamation: words, spoken or written, making imputations to harm the reputation of a person, with ten statutory exceptions.",
                simplifiedExplanationBn = "কারো সামাজিক সুনাম বিনষ্ট করার উদ্দেশ্যে মিথ্যা অপবাদ বা বক্তব্য ছড়ালে ৪৯৯ ধারায় মানহানি বলে গণ্য হয়। সত্য কথা যদি জনস্বার্থে প্রকাশ করা হয়, তবে তা মানহানির অপরাধ হবে না। এর শাস্তি নির্ধারিত ৫০০ ধারায়।",
                userNotes = null,
                punishmentSummaryBn = "শাস্তি ৫০০ ধারা অনুযায়ী নির্ধারিত",
                bailableStatus = "জামিনযোগ্য",
                cognizableStatus = "আমল-অযোগ্য (নালিশি মামলা করতে হয়)",
                trialCourt = "ম্যাজিস্ট্রেট আদালত",
                orderIndex = 12
            ),
            SectionEntity(
                id = 213,
                lawId = 2,
                chapterId = 14,
                sectionNumberBn = "৫০০",
                sectionNumberEn = "500",
                titleBn = "মানহানির শাস্তি (Punishment for defamation)",
                titleEn = "Punishment for defamation",
                fullContentBn = "যে ব্যক্তি অন্যের মানহানি করে, সেই ব্যক্তি অনধিক দুই বৎসর পর্যন্ত যেকোনো মেয়াদের বিনাশ্রম কারাদণ্ডে, অথবা অর্থদণ্ডে, অথবা উভয়বিধ দণ্ডেই দণ্ডিত হইবে।",
                fullContentEn = "Whoever defames another shall be punished with simple imprisonment for a term which may extend to two years, or with fine, or with both.",
                simplifiedExplanationBn = "মানহানির অভিযোগের জন্য ম্যাজিস্ট্রেট আদালতে সি.আর. (Complaint Register) মামলা দায়ের করতে হয়। দোষী সাব্যস্ত হলে আদালত সর্বোচ্চ ২ বছরের বিনাশ্রম কারাদণ্ড বা জরিমানা দিতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "অনধিক ২ বছর বিনাশ্রম কারাদণ্ড অথবা অর্থদণ্ড বা উভয় দণ্ড",
                bailableStatus = "জামিনযোগ্য (Bailable)",
                cognizableStatus = "আমল-অযোগ্য (Non-cognizable)",
                trialCourt = "প্রথম শ্রেণির ম্যাজিস্ট্রেট আদালত",
                orderIndex = 13
            ),

            // ============================================
            // 3. THE CODE OF CRIMINAL PROCEDURE (ফৌজদারি কার্যবিধি)
            // ============================================
            SectionEntity(
                id = 301,
                lawId = 3,
                chapterId = 15,
                sectionNumberBn = "৫৪",
                sectionNumberEn = "54",
                titleBn = "পুলিশ যখন পরোয়ানা ব্যতিরেকে গ্রেপ্তার করিতে পারে",
                titleEn = "When police may arrest without warrant",
                fullContentBn = """যেকোনো পুলিশ অফিসার কোনো ম্যাজিস্ট্রেটের আদেশ ব্যতীত এবং কোনো পরোয়ানা ছাড়াই নিম্নবর্ণিত ব্যক্তিকে গ্রেপ্তার করিতে পারেন:
১. যে ব্যক্তি কোনো আমলযোগ্য অপরাধে জড়িত বলিয়া যুক্তিসঙ্গত অভিযোগ বা বিশ্বাসযোগ্য সংবাদ রহিয়াছে;
২. যাহার নিকট আইনানুগ অজুহাত ব্যতিরেকে ঘর ভাঙার সরঞ্জাম পাওয়া যায়;
৩. যে ব্যক্তিকে অপরাধী বা ফেরার ঘোষণা করা হইয়াছে;
৪. যাহার দখলে কোনো চোরাই মাল বলিয়া যুক্তিসঙ্গত সন্দেহ হয়;
৫. যে ব্যক্তি পুলিশ অফিসারকে দায়িত্ব পালনে বাধা প্রদান করে বা হেফাজত হইতে পলায়ন করে;
৬. সশস্ত্র বাহিনী হইতে পলায়নকারী ব্যক্তি;
৭. কোনো প্রত্যর্পণযোগ্য অপরাধে জড়িত ব্যক্তি ইত্যাদি।""",
                fullContentEn = "Any police officer may, without an order from a Magistrate and without a warrant, arrest any person who has been concerned in any cognizable offence, or against whom a reasonable complaint has been made.",
                simplifiedExplanationBn = "বহুল আলোচিত ৫৪ ধারা। সুপ্রিম কোর্টের ব্লাস্ট বনাম বাংলাদেশ (BLAST vs Bangladesh) ঐতিহাসিক রায়ের নির্দেশনা অনুযায়ী, পুলিশ ইচ্ছেমতো যে কাউকে গ্রেপ্তার করতে পারবে না। গ্রেপ্তারের সুনির্দিষ্ট কারণ লিপিবদ্ধ করতে হবে এবং গ্রেপ্তারের ৩ ঘণ্টার মধ্যে আত্মীয়কে অবহিত করতে হবে।",
                userNotes = null,
                punishmentSummaryBn = "আইনসম্মত পদ্ধতি লঙ্ঘন হলে পুলিশি গ্রেপ্তার অবৈধ ঘোষিত হয়",
                bailableStatus = "কার্যপদ্ধতিগত বিধান",
                cognizableStatus = "পুলিশি ক্ষমতা",
                trialCourt = "ম্যাজিস্ট্রেট আদালত",
                orderIndex = 1
            ),
            SectionEntity(
                id = 302,
                lawId = 3,
                chapterId = 16,
                sectionNumberBn = "১৪৪",
                sectionNumberEn = "144",
                titleBn = "উপদ্রব বা আশঙ্কিত বিপদের জরুরি ক্ষেত্রে তাত্ক্ষণিক আদেশ জারির ক্ষমতা",
                titleEn = "Power to issue order absolute at once in urgent cases of nuisance or apprehended danger",
                fullContentBn = "যেখানে জেলা ম্যাজিস্ট্রেট বা নির্বাহী ম্যাজিস্ট্রেট বিবেচনা করেন যে মানুষের জীবন, স্বাস্থ্য বা নিরাপত্তার বিঘ্ন বা দাঙ্গা-হাঙ্গামা রোধকল্পে তাত্ক্ষণিক ব্যবস্থা গ্রহণ আবশ্যক, সেখানে তিনি কোনো নির্দিষ্ট এলাকায় জনসাধারণের চলাফেরা, সমাবেশ বা অস্ত্র বহন নিষিদ্ধ করিয়া লিখিত আদেশ জারি করিতে পারেন। এই আদেশের মেয়াদ সাধারণ ক্ষেত্রে দুই মাসের অধিক স্থায়ী হইবে না।",
                fullContentEn = "Power to issue temporary prohibitive orders in urgent cases of apprehended danger or breach of peace for a period not exceeding two months.",
                simplifiedExplanationBn = "জনশৃঙ্খলা বা শান্তি বজায় রাখতে ১৪৪ ধারা জারি করা হয়। এর অধীনে সমাবেশ, মাইকিং, বা পাঁচজনের অধিক জমায়েত নিষিদ্ধ করা যায়। সাধারণ মানুষ বা রাজনৈতিক দল এই আদেশ অমান্য করলে দণ্ডবিধির ১৮৮ ধারায় শাস্তির আওতায় আসে।",
                userNotes = null,
                punishmentSummaryBn = "আদেশ অমান্য করলে দণ্ডবিধির ১৮৮ ধারায় শাস্তি",
                bailableStatus = "নির্বাহী আদেশ",
                cognizableStatus = "নির্বাহী ম্যাজিস্ট্রেটের এখতিয়ার",
                trialCourt = "নির্বাহী ম্যাজিস্ট্রেট আদালত",
                orderIndex = 2
            ),
            SectionEntity(
                id = 303,
                lawId = 3,
                chapterId = 17,
                sectionNumberBn = "১৫৪",
                sectionNumberEn = "154",
                titleBn = "আমলযোগ্য অপরাধের সংবাদ (এজাহার / FIR)",
                titleEn = "Information in cognizable cases",
                fullContentBn = "থানার ভারপ্রাপ্ত কর্মকর্তার (OC) নিকট কোনো আমলযোগ্য অপরাধ সংঘটন সম্পর্কিত প্রত্যেকটি সংবাদ মৌখিক দেওয়া হইলে তিনি তাহা লিপিবদ্ধ করাইবেন এবং সংবাদদাতাকে পড়িয়া শুনাইবেন এবং সংবাদদাতার স্বাক্ষর গ্রহণ করিবেন। সেই সংবাদের মূল বিবরণী সরকার কর্তৃক নির্ধারিত বহিতে লিপিবদ্ধ করিতে হইবে।",
                fullContentEn = "Every information relating to the commission of a cognizable offence if given orally to an officer in charge of a police station shall be reduced to writing by him and signed by the informant.",
                simplifiedExplanationBn = "ফৌজদারি মামলার সূচনা হয় এই ১৫৪ ধারায় দেওয়া তথ্যের মাধ্যমে, যাকে আমরা সাধারণ ভাষায় FIR (First Information Report) বা এজাহার বলি। এজাহার পাওয়া মাত্রই পুলিশ ম্যাজিস্ট্রেটের অনুমতি ছাড়াই তদন্ত শুরু করতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "ফৌজদারি মামলার আনুষ্ঠানিক সূচনা",
                bailableStatus = "কার্যপ্রণালী",
                cognizableStatus = "আমলযোগ্য মামলায় বাধ্যতামূলক",
                trialCourt = "থানা ও ম্যাজিস্ট্রেট আদালত",
                orderIndex = 3
            ),
            SectionEntity(
                id = 304,
                lawId = 3,
                chapterId = 17,
                sectionNumberBn = "১৬১",
                sectionNumberEn = "161",
                titleBn = "পুলিশ কর্তৃক সাক্ষীদের জবানবন্দি গ্রহণ",
                titleEn = "Examination of witnesses by police",
                fullContentBn = "তদন্তকারী পুলিশ অফিসার ঘটনার তথ্য সম্পর্কে অবগত বলিয়া মনে করা যেকোনো ব্যক্তিকে মৌখিকভাবে জিজ্ঞাসাবাদ করিতে পারেন এবং সেই ব্যক্তি আত্মঘাতী প্রশ্ন ব্যতিরেকে পুলিশের সকল প্রশ্নের সত্য উত্তর দিতে বাধ্য থাকিবেন। পুলিশ অফিসার চাইলে উক্ত জবানবন্দি পৃথকভাবে লিপিবদ্ধ করিতে পারেন।",
                fullContentEn = "Any police officer investigating may examine orally any person supposed to be acquainted with the facts and circumstances of the case.",
                simplifiedExplanationBn = "পুলিশ তদন্ত চলাকালে সাক্ষীদের যে জবানবন্দি নেয় তা ১৬১ জবানবন্দি নামে পরিচিত। মনে রাখা দরকার, ১৬১ ধারার জবানবন্দিতে সাক্ষীর স্বাক্ষর নেওয়া নিষিদ্ধ এবং এই জবানবন্দি আদালতে সরাসরি প্রমাণ হিসেবে ব্যবহার করা যায় না, তবে সাক্ষীর বক্তব্য খণ্ডন (Contradiction) করতে ব্যবহার করা যায়।",
                userNotes = null,
                punishmentSummaryBn = "সাক্ষীর স্বাক্ষর গ্রহণ সম্পূর্ণ নিষিদ্ধ",
                bailableStatus = "তদন্ত কার্যপদ্ধতি",
                cognizableStatus = "পুলিশি ক্ষমতা",
                trialCourt = "বিচারিক আদালত",
                orderIndex = 4
            ),
            SectionEntity(
                id = 305,
                lawId = 3,
                chapterId = 17,
                sectionNumberBn = "১৬৪",
                sectionNumberEn = "164",
                titleBn = "ম্যাজিস্ট্রেট কর্তৃক স্বীকারোক্তি ও জবানবন্দি লিপিবদ্ধকরণ",
                titleEn = "Power to record statements and confessions",
                fullContentBn = "কোনো মেট্রোপলিটন ম্যাজিস্ট্রেট বা প্রথম শ্রেণির ম্যাজিস্ট্রেট তদন্ত চলাকালে যেকোনো সময় কোনো ব্যক্তির স্বীকারোক্তি (Confession) বা বিবৃতি লিপিবদ্ধ করিতে পারেন। ম্যাজিস্ট্রেট আসামীকে সতর্ক করিবেন যে তিনি স্বীকারোক্তি দিতে বাধ্য নহেন এবং স্বীকারোক্তি দিলে তাহা তাহার বিরুদ্ধে সাক্ষ্য হিসেবে ব্যবহৃত হইতে পারে।",
                fullContentEn = "Any Metropolitan Magistrate or Magistrate of the first class may record any statement or confession made to him in the course of an investigation.",
                simplifiedExplanationBn = "১৬৪ ধারার স্বীকারোক্তিমূলক জবানবন্দি ফৌজদারি বিচারে আসামির বিরুদ্ধে অত্যন্ত মারাত্মক সাক্ষ্য। ম্যাজিস্ট্রেটকে অবশ্যই নিশ্চিত হতে হবে যে আসামী সম্পূর্ণ স্বেচ্ছায় (Voluntary) স্বীকারোক্তি দিচ্ছে, কোনো পুলিশি ভয়ভীতি বা প্রলোভনে নয়। আসামিকে চিন্তা করার জন্য পর্যাপ্ত সময় দেওয়া বাধ্যতামূলক।",
                userNotes = null,
                punishmentSummaryBn = "আদালতে আসামির বিরুদ্ধে সরাসরি প্রমাণযোগ্য সাক্ষ্য",
                bailableStatus = "বিচারিক কার্যপদ্ধতি",
                cognizableStatus = "ম্যাজিস্ট্রেটের এখতিয়ার",
                trialCourt = "বিচারিক ম্যাজিস্ট্রেট আদালত",
                orderIndex = 5
            ),
            SectionEntity(
                id = 306,
                lawId = 3,
                chapterId = 17,
                sectionNumberBn = "১৬৭",
                sectionNumberEn = "167",
                titleBn = "তদন্ত ২৪ ঘণ্টার মধ্যে শেষ না হইলে কার্যপদ্ধতি (পুলিশ রিমান্ড)",
                titleEn = "Procedure when investigation cannot be completed in twenty-four hours (Remand)",
                fullContentBn = "যদি গ্রেপ্তারকৃত কোনো ব্যক্তির বিরুদ্ধে তদন্ত সংবিধান ও ২৪ ঘণ্টার মধ্যে সম্পন্ন করা না যায়, তবে পুলিশ কর্মকর্তা ম্যাজিস্ট্রেট বরাবর আবেদন করিবেন। ম্যাজিস্ট্রেট সন্তুষ্ট হইলে আসামিকে অনধিক ১৫ দিন পর্যন্ত সময়ে সময়ে পুলিশ হেফাজতে বা জেল হেফাজতে আটকের আদেশ দান করিতে পারেন।",
                fullContentEn = "Whenever investigation cannot be completed in 24 hours, Magistrate may authorize detention in police or judicial custody for a term not exceeding fifteen days on the whole.",
                simplifiedExplanationBn = "এটি বহুল আলোচিত পুলিশ রিমান্ড (Remand)-এর বিধান। পুলিশি রিমান্ড চাইতে হলে সুস্পষ্ট যুক্তি থাকতে হবে। সুপ্রিম কোর্টের ব্লাস্ট রায়ের নির্দেশনা অনুসারে রিমান্ডে শারীরিক নির্যাতন সম্পূর্ণ নিষিদ্ধ এবং আসামিকে কাঁচের তৈরি ঘরে বা আইনজীবীর উপস্থিতিতে জিজ্ঞাসাবাদ করার বিধান রয়েছে।",
                userNotes = null,
                punishmentSummaryBn = "সর্বোচ্চ ১৫ দিন পর্যন্ত পুলিশ রিমান্ডের অনুমতি দেওয়া যায়",
                bailableStatus = "হেফাজতের প্রশ্ন",
                cognizableStatus = "ম্যাজিস্ট্রেটের বিচারিক ক্ষমতা",
                trialCourt = "ম্যাজিস্ট্রেট আদালত",
                orderIndex = 6
            ),
            SectionEntity(
                id = 307,
                lawId = 3,
                chapterId = 19,
                sectionNumberBn = "৪৯৭",
                sectionNumberEn = "497",
                titleBn = "অ-জামিনযোগ্য অপরাধের ক্ষেত্রে কখন জামিন মঞ্জুর করা যাইবে",
                titleEn = "When bail may be taken in case of non-bailable offence",
                fullContentBn = """কোনো ব্যক্তি যখন অ-জামিনযোগ্য অপরাধের দায়ে অভিযুক্ত হইয়া আদালতে হাজির হন বা আনীত হন, তখন তাহাকে জামিনে মুক্তি দেওয়া যাইতে পারে।
তবে শর্ত থাকে যে, কোনো ব্যক্তি যদি মৃত্যুদণ্ড বা যাবজ্জীবন কারাদণ্ডে দণ্ডনীয় অপরাধে দোষী বলিয়া যুক্তিসঙ্গত বিশ্বাস থাকে, তবে তাহাকে জামিনে মুক্তি দেওয়া হইবে না।
আরও শর্ত থাকে যে, অভিযুক্ত ব্যক্তি যদি ষোল বৎসরের কম বয়স্ক বালক, কোনো স্ত্রীলোক বা কোনো অসুস্থ বা রুগ্ন ব্যক্তি হন, তবে আদালত তাহাকে জামিনে মুক্তি দিতে পারেন।""",
                fullContentEn = "When any person accused of any non-bailable offence is arrested or detained, he may be released on bail, with statutory relaxation for women, sick or minors.",
                simplifiedExplanationBn = "আদালতে জামিন শুনানির প্রধান ভিত্তি ধারা ৪৯৭। অ-জামিনযোগ্য মামলায় জামিন পাওয়া নাগরিকের অধিকার নয়, বরং আদালতের সুবিবেচনামূলক ক্ষমতা (Discretionary Power)। তবে নারী, অসুস্থ ব্যক্তি ও শিশুদের ক্ষেত্রে আইনে জামিনের বিশেষ নমনীয়তা দেওয়া হয়েছে।",
                userNotes = null,
                punishmentSummaryBn = "আদালত শর্তসাপেক্ষে বন্ডে জামিন প্রদান করতে পারে",
                bailableStatus = "আদালতের বিবেচনামূলক ক্ষমতা",
                cognizableStatus = "জামিন শুনানি",
                trialCourt = "ম্যাজিস্ট্রেট ও দায়রা আদালত",
                orderIndex = 7
            ),
            SectionEntity(
                id = 308,
                lawId = 3,
                chapterId = 19,
                sectionNumberBn = "৪৯৮",
                sectionNumberEn = "498",
                titleBn = "জামিন মঞ্জুর ও জামিনের পরিমাণ হ্রাস করিবার ক্ষমতা (অগ্রিম জামিন / Anticipatory Bail)",
                titleEn = "Power to direct admission to bail or reduction of bail",
                fullContentBn = "হাইকোর্ট বিভাগ বা দায়রা আদালত যেকোনো ক্ষেত্রে যেকোনো ব্যক্তিকে জামিনে মুক্তির নির্দেশ দান করিতে পারেন, বা কোনো ম্যাজিস্ট্রেট কর্তৃক তলবকৃত জামিনের অর্থের পরিমাণ হ্রাস করিতে পারেন।",
                fullContentEn = "High Court Division or Court of Session may in any case direct that any person be admitted to bail or that the bail required by a police-officer or Magistrate be reduced.",
                simplifiedExplanationBn = "হাইকোর্ট বিভাগ থেকে অগ্রিম জামিন (Anticipatory Bail) নেওয়ার মূল আইনি ভিত্তি ৪৯৮ ধারা। যদি কোনো নির্দোষ ব্যক্তি যুক্তিসঙ্গতভাবে আশঙ্কা করেন যে মিথ্যা বা হয়রানিমূলক মামলায় তাকে গ্রেপ্তার করা হতে পারে, তবে তিনি গ্রেপ্তার হওয়ার পূর্বেই সরাসরি হাইকোর্টে উপস্থিত হয়ে অগ্রিম জামিন চাইতে পারেন।",
                userNotes = null,
                punishmentSummaryBn = "গ্রেপ্তারের পূর্বে বা পরে উচ্চ আদালতের বিশেষ জামিন ক্ষমতা",
                bailableStatus = "উচ্চ আদালতের বিশেষ ক্ষমতা",
                cognizableStatus = "দায়রা ও হাইকোর্ট বিভাগ",
                trialCourt = "দায়রা জজ আদালত ও হাইকোর্ট বিভাগ",
                orderIndex = 8
            ),
            SectionEntity(
                id = 309,
                lawId = 3,
                chapterId = 20,
                sectionNumberBn = "৫৬১ক",
                sectionNumberEn = "561A",
                titleBn = "হাইকোর্ট বিভাগের সহজাত ক্ষমতা (Inherent power of High Court Division / Quashment)",
                titleEn = "Saving of inherent power of High Court Division",
                fullContentBn = "এই সংহিতার কোনো কিছুই হাইকোর্ট বিভাগের এমন কোনো আদেশ দানের সহজাত ক্ষমতাকে সীমাবদ্ধ বা ক্ষতিগ্রস্ত করিবে না যাহা এই সংহিতার অধীন কোনো আদেশ কার্যকর করিবার জন্য, অথবা কোনো আদালতের বিচারিক প্রক্রিয়ার অপব্যবহার রোধ করিবার জন্য, অথবা অন্যভাবে ন্যায়বিচার নিশ্চিত করিবার উদ্দেশ্যে আবশ্যক হয়।",
                fullContentEn = "Nothing in this Code shall be deemed to limit or affect the inherent power of the High Court Division to make such orders as may be necessary to give effect to any order under this Code, or to prevent abuse of the process of any Court or otherwise to secure the ends of justice.",
                simplifiedExplanationBn = "ফৌজদারি বিচারব্যবস্থায় ন্যায়বিচার নিশ্চিতের ব্রহ্মাস্ত্র ৫৬১ক ধারা। কোনো ব্যক্তির বিরুদ্ধে সম্পূর্ণ মিথ্যা, ভিত্তিহীন বা ব্ল্যাকমেইলিংয়ের উদ্দেশ্যে মামলা হলে এবং তাতে বিচারিক প্রক্রিয়ার সুস্পষ্ট অপব্যবহার (Abuse of Process) দেখা দিলে, হাইকোর্ট বিভাগ ৫৬১ক ধারায় মামলাটি সম্পূর্ণ বাতিল (Quashment) করে দিতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "মামলার বিচার কার্যক্রম বা এফআইআর সরাসরি বাতিল করার ক্ষমতা",
                bailableStatus = "সহজাত এখতিয়ার",
                cognizableStatus = "হাইকোর্ট বিভাগের ক্ষমতা",
                trialCourt = "হাইকোর্ট বিভাগ, বাংলাদেশ সুপ্রিম কোর্ট",
                orderIndex = 9
            ),

            // ============================================
            // 4. THE EVIDENCE ACT, 1872 (সাক্ষ্য আইন, ১৮৭২)
            // ============================================
            SectionEntity(
                id = 401,
                lawId = 4,
                chapterId = 22,
                sectionNumberBn = "২৫",
                sectionNumberEn = "25",
                titleBn = "পুলিশ কর্মকর্তার নিকট স্বীকারোক্তি প্রমাণ করা যাইবে না",
                titleEn = "Confession to police officer not to be proved",
                fullContentBn = "কোনো পুলিশ অফিসারের নিকট কোনো অপরাধে অভিযুক্ত ব্যক্তি কর্তৃক প্রদত্ত কোনো স্বীকারোক্তি সেই ব্যক্তির বিরুদ্ধে প্রমাণ হিসেবে গ্রহণ করা যাইবে না।",
                fullContentEn = "No confession made to a police-officer, shall be proved as against a person accused of any offence.",
                simplifiedExplanationBn = "আসামিকে দিয়ে জোরপূর্বক বা ভয়ভীতি দেখিয়ে স্বীকারোক্তি আদায় বন্ধ করতে এই কঠোর আইনি সুরক্ষা দেওয়া হয়েছে। পুলিশ রিমান্ডে বা থানায় আসামি পুলিশের কাছে কোনো জবানবন্দি দিলে তা আদালতে অপরাধ প্রমাণের কোনো মূল্য বহন করে না।",
                userNotes = null,
                punishmentSummaryBn = "পুলিশের নিকট দেওয়া স্বীকারোক্তি আদালতে সম্পূর্ণ অগ্রাহ্য",
                bailableStatus = "সাক্ষ্যের গ্রহণযোগ্যতা",
                cognizableStatus = "প্রমাণের অযোগ্য",
                trialCourt = "সকল ফৌজদারি আদালত",
                orderIndex = 1
            ),
            SectionEntity(
                id = 402,
                lawId = 4,
                chapterId = 22,
                sectionNumberBn = "২৭",
                sectionNumberEn = "27",
                titleBn = "আসামির নিকট হইতে প্রাপ্ত তথ্যের ভিত্তিতে কতখানি প্রমাণ করা যাইবে",
                titleEn = "How much of information received from accused may be proved",
                fullContentBn = "তবে শর্ত থাকে যে, কোনো অপরাধে অভিযুক্ত ব্যক্তি যখন পুলিশ হেফাজতে থাকে, তখন তাহার নিকট হইতে প্রাপ্ত তথ্যের ফলস্বরূপ কোনো বস্তু বা আলামত যদি উদ্ধার ও আবিষ্কৃত হয়, তাহা হইলে সেই তথ্যের যতটুকু উক্ত আবিষ্কারের সহিত সুস্পষ্টভাবে সম্পর্কিত কেবল ততটুকু আদালতে প্রমাণ করা যাইবে।",
                fullContentEn = "When any fact is deposed to as discovered in consequence of information received from a person accused in police custody, so much of such information as relates distinctly to the fact thereby discovered may be proved.",
                simplifiedExplanationBn = "২৫ ধারার একমাত্র প্রধান ব্যতিক্রম এটি। আসামি পুলিশকে যে তথ্য দিয়েছে তার ভিত্তিতে যদি হত্যাকাণ্ডে ব্যবহৃত ছুরি, বন্দুক বা চোরাই মালামাল সরাসরি উদ্ধার করা হয়, তাহলে কেবল ওই উদ্ধারের অংশটুকু আদালতে প্রমাণ হিসেবে গ্রহণযোগ্য হবে।",
                userNotes = null,
                punishmentSummaryBn = "আলামত আবিষ্কারের সাথে সরাসরি সম্পর্কিত অংশটুকু গ্রহণযোগ্য",
                bailableStatus = "সাক্ষ্য আইন",
                cognizableStatus = "প্রাসঙ্গিক সাক্ষ্য",
                trialCourt = "বিচারিক আদালত",
                orderIndex = 2
            ),
            SectionEntity(
                id = 403,
                lawId = 4,
                chapterId = 22,
                sectionNumberBn = "৩২",
                sectionNumberEn = "32",
                titleBn = "যেসব ব্যক্তির মৃত্যু ঘটিয়াছে তাহাদের বিবৃতি (মৃত্যুকালীন ঘোষণা / Dying Declaration)",
                titleEn = "Cases in which statement of relevant fact by person who is dead or cannot be found, etc., is relevant",
                fullContentBn = "যখন কোনো বিবৃতি কোনো ব্যক্তি কর্তৃক তাহার মৃত্যুর কারণ সম্পর্কে প্রদান করা হয়, অথবা যে পরিস্থিতিতে তাহার মৃত্যু ঘটিয়াছিল সেই সম্পর্কে করা হয়, তখন সেই ব্যক্তির মৃত্যুর কারণ যে মোকদ্দমায় প্রশ্নের বিষয় সেখানে সেই বিবৃতি অত্যন্ত প্রাসঙ্গিক ও গ্রহণযোগ্য।",
                fullContentEn = "Statements, written or verbal, of relevant facts made by a person who is dead, relating to cause of death (Dying Declaration).",
                simplifiedExplanationBn = "মৃত্যুকালীন ঘোষণা (Dying Declaration)। একজন মৃত্যুপথযাত্রী মানুষ মিথ্যা অপবাদ দিয়ে মৃত্যুবরণ করবেন না—এই আইনের অনুমানের ওপর ভিত্তি করে এটি অত্যন্ত জোরালো সাক্ষ্য। কোনো ব্যক্তি মৃত্যুর আগে তার ওপর আক্রমণের ঘটনা বর্ণনা করে গেলে তা কোনো জেরা ছাড়াই দোষী সাব্যস্ত করার জন্য যথেষ্ট হতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "হত্যার বিচারে অত্যন্ত নির্ভরযোগ্য সাক্ষ্য হিসেবে গণ্য",
                bailableStatus = "সাক্ষ্য আইন",
                cognizableStatus = "গ্রহণযোগ্য প্রমাণ",
                trialCourt = "দায়রা আদালত",
                orderIndex = 3
            ),
            SectionEntity(
                id = 404,
                lawId = 4,
                chapterId = 23,
                sectionNumberBn = "১০১",
                sectionNumberEn = "101",
                titleBn = "প্রমাণের দায়ভার (Burden of proof)",
                titleEn = "Burden of proof",
                fullContentBn = "যে ব্যক্তি আদালতের নিকট ইচ্ছা প্রকাশ করে যে আদালত তাহার বর্ণিত তথ্যের অস্তিত্বের ওপর ভিত্তি করিয়া কোনো আইনগত অধিকার বা দায় সাব্যস্ত করিবেন, সেই ব্যক্তিকে অবশ্যই উক্ত তথ্যাবলীর অস্তিত্ব প্রমাণ করিতে হইবে।",
                fullContentEn = "Whoever desires any Court to give judgment as to any legal right or liability dependent on the existence of facts which he asserts, must prove that those facts exist.",
                simplifiedExplanationBn = "আইনের সর্বজনীন নীতি: 'যিনি দাবি করবেন, প্রমাণ করার দায় তারই' (He who asserts must prove)। ফৌজদারি মামলায় রাষ্ট্রপক্ষ বা বাদীপক্ষকে আসামির অপরাধ সন্দেহাতীতভাবে প্রমাণ করতে হয়। আসামি নির্দোষ প্রমাণ না করলেও চলে, যতক্ষণ না রাষ্ট্রপক্ষ নিজের অভিযোগ প্রমাণ করে।",
                userNotes = null,
                punishmentSummaryBn = "বাদী বা রাষ্ট্রপক্ষের ওপর প্রমাণের মূল দায়িত্ব",
                bailableStatus = "আইনি নীতি",
                cognizableStatus = "বিচারিক নীতি",
                trialCourt = "সকল দেওয়ানি ও ফৌজদারি আদালত",
                orderIndex = 4
            ),

            // ============================================
            // 5. THE CODE OF CIVIL PROCEDURE (দেওয়ানি কার্যবিধি)
            // ============================================
            SectionEntity(
                id = 501,
                lawId = 5,
                chapterId = 25,
                sectionNumberBn = "৯",
                sectionNumberEn = "9",
                titleBn = "আদালতসমূহ সকল দেওয়ানি মোকদ্দমার বিচার করিবে যতক্ষণ না স্পষ্টভাবে নিষিদ্ধ হয়",
                titleEn = "Courts to try all civil suits unless barred",
                fullContentBn = "আদালতসমূহ দেওয়ানি প্রকৃতির সকল মোকদ্দমা বিচারের এখতিয়ার রাখিবেন, তবে যেসকল মোকদ্দমা গ্রহণ কোনো আইন দ্বারা সুস্পষ্ট বা পরোক্ষভাবে বারিত বা নিষিদ্ধ করা হইয়াছে সেইগুলি ছাড়া।",
                fullContentEn = "The Courts shall have jurisdiction to try all suits of a civil nature excepting suits of which their cognizance is either expressly or impliedly barred.",
                simplifiedExplanationBn = "দেওয়ানি আদালতের ব্যাপক এখতিয়ার নির্দেশক ধারা। জমিজমা, স্বত্ব, উত্তরাধিকার, চুক্তিভঙ্গ বা নাগরিক অধিকার সংক্রান্ত যেকোনো দেওয়ানি বিরোধ নিষ্পত্তির জন্য দেওয়ানি আদালত উন্মুক্ত, যদি না কোনো বিশেষ আইনে (যেমন পারিবারিক আদালত বা শ্রম ট্রাইব্যুনাল) তা অন্য আদালতে দেওয়া থাকে।",
                userNotes = null,
                punishmentSummaryBn = "দেওয়ানি মোকদ্দমা দায়েরের সর্বজনীন এখতিয়ার",
                bailableStatus = "দেওয়ানি এখতিয়ার",
                cognizableStatus = "দেওয়ানি প্রতিকার",
                trialCourt = "সহকারী জজ ও যুগ্ম জেলা জজ আদালত",
                orderIndex = 1
            ),
            SectionEntity(
                id = 502,
                lawId = 5,
                chapterId = 25,
                sectionNumberBn = "১১",
                sectionNumberEn = "11",
                titleBn = "রেস জুডিকাটা (Res Judicata / দোবারা দোষ)",
                titleEn = "Res judicata",
                fullContentBn = "কোনো আদালত এমন কোনো মোকদ্দমা বা বিষয়ের বিচার করিবেন না যাহা ইতিপূর্বে একই পক্ষগণের মধ্যে বা তাহাদের স্বার্থের প্রতিনিধির মধ্যে একই বিষয়ে উপযুক্ত এখতিয়ারসম্পন্ন কোনো আদালত কর্তৃক চূড়ান্তভাবে নিষ্পত্তি করা হইয়াছে।",
                fullContentEn = "No Court shall try any suit or issue in which the matter directly and substantially in issue has been directly and substantially in issue in a former suit between the same parties and has been heard and finally decided.",
                simplifiedExplanationBn = "একটি বিরোধে আদালত একবার চূড়ান্ত রায় দিয়ে ফেললে, একই পক্ষ ওই একই বিষয় নিয়ে দ্বিতীয়বার নতুন মামলা দায়ের করতে পারবে না। এটি মামলার দীর্ঘসূত্রতা রোধ ও আদালতের রায়কে চূড়ান্ত মর্যাদা দেওয়ার মূল স্তম্ভ।",
                userNotes = null,
                punishmentSummaryBn = "পরবর্তী মামলা সরাসরি খারিজ ও বাতিল হইবে",
                bailableStatus = "দেওয়ানি বিধি",
                cognizableStatus = "আইনগত বাধা",
                trialCourt = "দেওয়ানি আদালত",
                orderIndex = 2
            ),
            SectionEntity(
                id = 503,
                lawId = 5,
                chapterId = 27,
                sectionNumberBn = "১৫১",
                sectionNumberEn = "151",
                titleBn = "আদালতের সহজাত ক্ষমতা রক্ষা (Inherent powers of Court)",
                titleEn = "Saving of inherent powers of Court",
                fullContentBn = "এই সংহিতার কোনো কিছুই আদালতের এমন কোনো আদেশ দানের সহজাত ক্ষমতাকে সীমাবদ্ধ বা অন্যভাবে ক্ষতিগ্রস্ত করিবে না যাহা ন্যায়বিচার প্রতিষ্ঠার উদ্দেশ্যে অথবা আদালতের বিচারিক প্রক্রিয়ার অপব্যবহার রোধকল্পে প্রয়োজনীয় বলিয়া প্রতীয়মান হয়।",
                fullContentEn = "Nothing in this Code shall be deemed to limit or otherwise affect the inherent power of the Court to make such orders as may be necessary for the ends of justice or to prevent abuse of the process of the Court.",
                simplifiedExplanationBn = "দেওয়ানি আদালতের ১৫১ ধারা দেওয়ানি কার্যবিধির অন্যতম শক্তিশালী বিধান। কার্যবিধিতে যদি কোনো নির্দিষ্ট প্রতিকার সরাসরি বলা না থাকে, কিন্তু ন্যায়বিচারের স্বার্থে বা হয়রানি ঠেকাতে কোনো আদেশ দেওয়া জরুরি হয়, আদালত তার সহজাত ক্ষমতা প্রয়োগ করে আদেশ দিতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "ন্যায়বিচারের স্বার্থে যেকোনো উপযুক্ত অন্তর্বর্তী আদেশ দানের ক্ষমতা",
                bailableStatus = "সহজাত এখতিয়ার",
                cognizableStatus = "দেওয়ানি আদালতের ক্ষমতা",
                trialCourt = "দেওয়ানি আদালত",
                orderIndex = 3
            ),

            // ============================================
            // 6. SPECIFIC RELIEF ACT, 1877 (সুনির্দিষ্ট প্রতিকার আইন)
            // ============================================
            SectionEntity(
                id = 701,
                lawId = 7,
                chapterId = 28,
                sectionNumberBn = "৯",
                sectionNumberEn = "9",
                titleBn = "স্থাবর সম্পত্তি হইতে বেদখলকৃত ব্যক্তির মামলা",
                titleEn = "Suit by person dispossessed of immovable property",
                fullContentBn = "যদি কোনো ব্যক্তি তাহার সম্মতি ব্যতীত স্থাবর সম্পত্তি হইতে যথাযথ আইনানুগ পন্থা ছাড়া বেদখল হন, তাহা হইলে তিনি স্বত্ব বা মালিকানা সংক্রান্ত প্রশ্ন উত্থাপন ব্যতিরেকেই কেবল দখল প্রমাণের ভিত্তিতে বেদখল হওয়ার তারিখ হইতে ছয় মাসের মধ্যে মোকদ্দমা দায়ের করিয়া উক্ত সম্পত্তির দখল পুনরুদ্ধার করিতে পারিবেন।",
                fullContentEn = "If any person is dispossessed without his consent of immovable property otherwise than in due course of law, he may, by suit, recover possession thereof within 6 months, notwithstanding any other title that may be set up.",
                simplifiedExplanationBn = "জোরপূর্বক জমি দখলের বিরুদ্ধে এটি তাৎক্ষণিক আইনি প্রতিকার। জমিটির মূল মালিক কে তা এখানে মুখ্য নয়; যিনি দখলে ছিলেন তাকে গায়ের জোরে বের করে দেওয়া হলে, তিনি বেদখল হওয়ার ৬ মাসের মধ্যে ৯ ধারায় মামলা করে দখল ফেরত পেতে পারেন।",
                userNotes = null,
                punishmentSummaryBn = "স্বত্ব প্রমাণ ছাড়াই দখল ফিরিয়ে দেওয়ার ডিক্রি প্রদান",
                bailableStatus = "দেওয়ানি প্রতিকার",
                cognizableStatus = "৬ মাসের তামাদি মেয়াদ",
                trialCourt = "সহকারী জজ আদালত",
                orderIndex = 1
            ),
            SectionEntity(
                id = 702,
                lawId = 7,
                chapterId = 30,
                sectionNumberBn = "৩৯",
                sectionNumberEn = "39",
                titleBn = "কখন দলিল বাতিল করিবার আদেশ দেওয়া যাইতে পারে",
                titleEn = "When cancellation may be ordered",
                fullContentBn = "যেকোনো ব্যক্তি যাহার বিরুদ্ধে কোনো লিখিত দলিল অবৈধ বা বাতিলযোগ্য, এবং যাহার যুক্তিসঙ্গত আশঙ্কা রহিয়াছে যে উক্ত দলিল যদি বলবৎ থাকে তবে তাহা তাহার গুরুতর ক্ষতির কারণ হইবে, তিনি উক্ত দলিল বাতিল ঘোষণার জন্য আদালতের শরণাপন্ন হইতে পারেন।",
                fullContentEn = "Any person against whom a written instrument is void or voidable, and who has reasonable apprehension that such instrument may cause him serious injury, may sue to have it adjudged void or voidable.",
                simplifiedExplanationBn = "ভুয়া বা জালিয়াতির মাধ্যমে তৈরি দলিলের বিরুদ্ধে এটি মুখ্য প্রতিকার। জাল দলিল দিয়ে কেউ আপনার জমি দাবি করলে দেওয়ানি আদালতে ৩৯ ধারায় দলিল বাতিলের মামলা করে দলিলটি রেজিস্ট্রেশন অফিস থেকে প্রত্যাহারের ডিক্রি আদায় করা যায়।",
                userNotes = null,
                punishmentSummaryBn = "জাল বা প্রতারণামূলক দলিল আইনত বাতিল ঘোষণা ও বাজেয়াপ্তকরণ",
                bailableStatus = "দেওয়ানি মোকদ্দমা",
                cognizableStatus = "দেওয়ানি স্বত্ব",
                trialCourt = "যুগ্ম জেলা জজ আদালত",
                orderIndex = 2
            ),
            SectionEntity(
                id = 703,
                lawId = 7,
                chapterId = 31,
                sectionNumberBn = "৪২",
                sectionNumberEn = "42",
                titleBn = "আইনগত মর্যাদা বা অধিকার সম্পর্কে আদালতের ঘোষণামূলক ডিক্রি",
                titleEn = "Discretion of Court as to declaration of status or right",
                fullContentBn = "যেকোনো ব্যক্তি কোনো আইনগত চরিত্র বা কোনো সম্পত্তির অধিকারের অধিকারী হইলে তিনি এমন ব্যক্তির বিরুদ্ধে মোকদ্দমা দায়ের করিতে পারেন যিনি উক্ত চরিত্র বা অধিকারে অস্বীকৃতি জ্ঞাপন করিয়াছেন। তবে শর্ত থাকে যে, যেখানে বাদী ঘোষণার অতিরিক্ত প্রতিকার প্রার্থনা করিতে পারিতেন কিন্তু তাহা করেন নাই, আদালত সেখানে কেবল ঘোষণার ডিক্রি দান করিবেন না।",
                fullContentEn = "Discretion of Court as to declaration of legal character or right to any property, with proviso barring suit if plaintiff, being able to seek further relief, omits to do so.",
                simplifiedExplanationBn = "জমির মালিকানা বা স্বত্ব প্রতিষ্ঠার জন্য বহুল প্রচলিত 'স্বত্ব ঘোষণা'র মোকদ্দমা ৪২ ধারায় করা হয়। মনে রাখা জরুরি, যদি আপনি বেদখলে থাকেন তবে কেবল মালিকানা ঘোষণার মামলা করলে চলবে না, দখলের অতিরিক্ত প্রতিকারও চাইতে হবে।",
                userNotes = null,
                punishmentSummaryBn = "সম্পত্তির বৈধ মালিকানা বা আইনি মর্যাদার সরকারি আদালতের স্বীকৃতি",
                bailableStatus = "দেওয়ানি মামলা",
                cognizableStatus = "স্বত্ব সংক্রান্ত বিরোধ",
                trialCourt = "সহকারী জজ ও সিনিয়র সহকারী জজ আদালত",
                orderIndex = 3
            ),
            SectionEntity(
                id = 704,
                lawId = 7,
                chapterId = 32,
                sectionNumberBn = "৫৪",
                sectionNumberEn = "54",
                titleBn = "চিরতরে নিষেধাজ্ঞা কখন মঞ্জুর করা যাইতে পারে (Permanent Injunction)",
                titleEn = "Perpetual injunctions when granted",
                fullContentBn = "বিবাদী যখন বাদীর সম্পত্তির অধিকার বা ভোগের অধিকার লঙ্ঘন করে বা লঙ্ঘনের হুমকি দেয়, তখন আদালত নিম্নোক্ত ক্ষেত্রে চিরতরে নিষেধাজ্ঞা মঞ্জুর করিতে পারেন: যেখানে বিবাদী বাদীর সম্পত্তির ট্রাস্টি; যেখানে ক্ষতির কোনো আর্থিক পরিমাপ অসম্ভব; যেখানে আর্থিক ক্ষতিপূরণ পর্যাপ্ত নয়; এবং যেখানে বিচারিক প্রক্রিয়ার পুনরাবৃত্তি রোধ করা আবশ্যক।",
                fullContentEn = "Perpetual injunction may be granted to the plaintiff to prevent the breach of an obligation existing in his favour or invasion of property rights.",
                simplifiedExplanationBn = "আপনার জমিতে কেউ যেন কোনো অবৈধ স্থাপনা নির্মাণ করতে না পারে, জমি বিক্রি করতে না পারে বা শান্তিময় ভোগদখলে কোনো বিঘ্ন না ঘটায়, আদালত তার ওপর চিরতরে নিষেধাজ্ঞা দিয়ে বাদীর অধিকার অক্ষুণ্ণ রাখে।",
                userNotes = null,
                punishmentSummaryBn = "বিবাদীকে চিরতরে জমিতে প্রবেশ বা হস্তক্ষেপ থেকে নিষিদ্ধকরণ",
                bailableStatus = "দেওয়ানি প্রতিকার",
                cognizableStatus = "স্থায়ী নিষেধাজ্ঞা",
                trialCourt = "দেওয়ানি সহকারী জজ আদালত",
                orderIndex = 4
            ),

            // ============================================
            // 7. WOMEN & CHILDREN REPRESSION ACT, 2000
            // ============================================
            SectionEntity(
                id = 601,
                lawId = 6,
                chapterId = 33,
                sectionNumberBn = "৯",
                sectionNumberEn = "9",
                titleBn = "ধর্ষণ ও ধর্ষণজনিত কারণে মৃত্যুর শাস্তি",
                titleEn = "Punishment for rape and causing death after rape",
                fullContentBn = """(১) যদি কোনো পুরুষ কোনো নারী বা শিশুকে ধর্ষণ করেন, তাহা হইলে তিনি মৃত্যুদণ্ডে বা যাবজ্জীবন সশ্রম কারাদণ্ডে দণ্ডিত হইবেন এবং ইহার অতিরিক্ত অর্থদণ্ডেও দণ্ডিত হইবেন। (সংশোধিত ২০২০)
(২) যদি কোনো ব্যক্তি ধর্ষণ বা ধর্ষণের পর কৃত অপরাধের দ্বারা ধর্ষিতা নারী বা শিশুর মৃত্যু ঘটান, তাহা হইলে উক্ত ব্যক্তি মৃত্যুদণ্ডে বা যাবজ্জীবন সশ্রম কারাদণ্ডে দণ্ডিত হইবেন এবং ইহার অতিরিক্ত অনূর্ধ্ব এক লক্ষ টাকা অর্থদণ্ডেও দণ্ডিত হইবেন।
(৩) যদি একাধিক ব্যক্তি দলবদ্ধভাবে ধর্ষণ করেন এবং ধর্ষণের ফলে কোনো নারী বা শিশুর মৃত্যু ঘটে বা তিনি আহত হন, তাহা হইলে উক্ত দলের প্রত্যেক ব্যক্তি মৃত্যুদণ্ডে বা যাবজ্জীবন সশ্রম কারাদণ্ডে দণ্ডিত হইবেন।""",
                fullContentEn = "Punishment for rape, death caused after rape, or gang rape: punishable with death or imprisonment for life, and fine.",
                simplifiedExplanationBn = "২০২০ সালের সংশোধনী অনুযায়ী ধর্ষণের সর্বোচ্চ সাজা মৃত্যুদণ্ড করা হয়েছে। এই আইনের মামলা নারী ও শিশু নির্যাতন দমন বিশেষ ট্রাইব্যুনালে বিচার হয়। এটি সম্পূর্ণ অ-জামিনযোগ্য এবং আমলযোগ্য অপরাধ।",
                userNotes = null,
                punishmentSummaryBn = "মৃত্যুদণ্ড অথবা যাবজ্জীবন সশ্রম কারাদণ্ড এবং অর্থদণ্ড",
                bailableStatus = "অ-জামিনযোগ্য (Non-Bailable)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "নারী ও শিশু নির্যাতন দমন ট্রাইব্যুনাল",
                orderIndex = 1
            ),
            SectionEntity(
                id = 602,
                lawId = 6,
                chapterId = 33,
                sectionNumberBn = "১১",
                sectionNumberEn = "11",
                titleBn = "যৌতুকের জন্য মারপিট ও মৃত্যুর শাস্তি",
                titleEn = "Punishment for causing death or hurt for dowry",
                fullContentBn = """যদি কোনো নারীর স্বামী বা স্বামীর পিতা, মাতা, অভিভাবক বা আত্মীয় যৌতুকের জন্য উক্ত নারীর:
(ক) মৃত্যু ঘটান বা মৃত্যু ঘটানোর চেষ্টা করেন, তাহা হইলে মৃত্যুদণ্ডে বা যাবজ্জীবন কারাদণ্ডে দণ্ডিত হইবেন;
(খ) মারাত্মক জখম (Grievous Hurt) করেন, তাহা হইলে যাবজ্জীবন কারাদণ্ডে অথবা অনধিক বারো বৎসর এবং অন্যূন পাঁচ বৎসর সশ্রম কারাদণ্ডে দণ্ডিত হইবেন;
(গ) সাধারণ জখম (Simple Hurt) করেন, তাহা হইলে অনধিক তিন বৎসর কিন্তু অন্যূন এক বৎসর সশ্রম কারাদণ্ডে দণ্ডিত হইবেন।""",
                fullContentEn = "Punishment for dowry-related violence: death or life imprisonment for death, 5-12 years for grievous hurt, and 1-3 years for simple hurt.",
                simplifiedExplanationBn = "যৌতুকের দাবিতে নির্যাতনের বিরুদ্ধে এই বিশেষ ধারা। আদালতে এই ধারার অপব্যবহার রোধে সুপ্রিম কোর্টের কড়া নজরদারি রয়েছে। যৌতুকের দাবি ও মারপিটের সুস্পষ্ট সাক্ষ্যপ্রমাণ ছাড়া কেবল স্বামী বা পরিবারের সদস্যদের হয়রানির উদ্দেশ্যে মামলা প্রমাণিত হলে বাদীর বিরুদ্ধে ১৭ ধারায় পাল্টা সাজার বিধান রয়েছে।",
                userNotes = null,
                punishmentSummaryBn = "মৃত্যু ঘটালে মৃত্যুদণ্ড/যাবজ্জীবন; মারপিটের ক্ষেত্রে ১ থেকে ১২ বছর সশ্রম কারাদণ্ড",
                bailableStatus = "অ-জামিনযোগ্য (উপধারা গ আদালতের বিবেচনায় জামিন হতে পারে)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "নারী ও শিশু নির্যাতন দমন ট্রাইব্যুনাল",
                orderIndex = 2
            ),
            SectionEntity(
                id = 603,
                lawId = 6,
                chapterId = 33,
                sectionNumberBn = "১৭",
                sectionNumberEn = "17",
                titleBn = "মিথ্যা মামলা বা অভিযোগ দায়েরের শাস্তি",
                titleEn = "Punishment for lodging false case or complaint",
                fullContentBn = "যদি কোনো ব্যক্তি এই আইনের অধীন কোনো অপরাধ সংঘটিত হইয়াছে জানিয়াও কোনো ব্যক্তির ক্ষতিসাধনের উদ্দেশ্যে মিথ্যা বা ভিত্তিহীন মামলা বা অভিযোগ দায়ের করেন, তাহা হইলে সেই ব্যক্তি অনধিক সাত বৎসর সশ্রম কারাদণ্ডে দণ্ডিত হইবেন এবং তদুপরি অর্থদণ্ডেও দণ্ডিত হইবেন।",
                fullContentEn = "Whoever lodges a false complaint with intent to injure shall be punished with rigorous imprisonment for a term which may extend to seven years, and fine.",
                simplifiedExplanationBn = "নারী ও শিশু নির্যাতন দমন আইনের অপব্যবহার ঠেকাতে এটি রক্ষাকবচ। কেউ যদি শত্রুতা বা ব্ল্যাকমেইলের উদ্দেশ্যে মিথ্যা ধর্ষণের বা যৌতুকের মামলা করে, অভিযোগ অসত্য প্রমাণিত হলে আদালত মিথ্যা মামলার বাদীর বিরুদ্ধেই ৭ বছর পর্যন্ত সশ্রম কারাদণ্ডের আদেশ দিতে পারে।",
                userNotes = null,
                punishmentSummaryBn = "অনধিক ৭ বছর সশ্রম কারাদণ্ড এবং অর্থদণ্ড",
                bailableStatus = "অ-জামিনযোগ্য",
                cognizableStatus = "আমলযোগ্য",
                trialCourt = "নারী ও শিশু নির্যাতন দমন ট্রাইব্যুনাল",
                orderIndex = 3
            ),

            // ============================================
            // 8. MUSLIM FAMILY LAWS ORDINANCE, 1961
            // ============================================
            SectionEntity(
                id = 1101,
                lawId = 11,
                chapterId = 37,
                sectionNumberBn = "৬",
                sectionNumberEn = "6",
                titleBn = "বহুবিবাহ (Polygamy) ও সালিসি পরিষদের পূর্বানুমোদন",
                titleEn = "Polygamy and Arbitration Council permission",
                fullContentBn = """(১) সালিসি পরিষদের পূর্বানুমতি লিখিতভাবে গ্রহণ ব্যতীত কোনো পুরুষ বলবৎ বিবাহ বিদ্যমান থাকা অবস্থায় অপর কোনো বিবাহ বন্ধনে আবদ্ধ হইতে পারিবেন না।
(২) উক্ত অনুমতি ব্যতিরেকে কোনো বিবাহ সম্পন্ন হইলে উক্ত বিবাহ রেজিস্ট্রি হইবে না।
(৩) এই বিধান লঙ্ঘনকারী পুরুষ অবিলম্বে তাহার বর্তমান স্ত্রী বা স্ত্রীদের সম্পূর্ণ দেনমোহর পরিশোধ করিতে বাধ্য থাকিবেন এবং অনধিক এক বৎসর পর্যন্ত কারাদণ্ডে বা দশ হাজার টাকা পর্যন্ত অর্থদণ্ডে বা উভয় দণ্ডেই দণ্ডিত হইবেন।""",
                fullContentEn = "No man shall contract another marriage during the subsistence of an existing marriage except with the previous permission in writing of the Arbitration Council.",
                simplifiedExplanationBn = "প্রথম স্ত্রী বিদ্যমান থাকা অবস্থায় সালিসি পরিষদের অনুমতি না নিয়ে দ্বিতীয় বিয়ে করা শাস্তিযোগ্য অপরাধ। আইন ভঙ্গ করলে স্বামী অবিলম্বে প্রথম স্ত্রীর পুরো দেনমোহর দিতে বাধ্য থাকবেন এবং ১ বছর কারাদণ্ডে দণ্ডিত হবেন।",
                userNotes = null,
                punishmentSummaryBn = "১ বছর পর্যন্ত কারাদণ্ড অথবা ১০ হাজার টাকা জরিমানা এবং দেনমোহর অবিলম্বে পরিশোধ",
                bailableStatus = "জামিনযোগ্য",
                cognizableStatus = "আমল-অযোগ্য (নালিশি মামলা)",
                trialCourt = "প্রথম শ্রেণির ম্যাজিস্ট্রেট আদালত / পারিবারিক আদালত",
                orderIndex = 1
            ),
            SectionEntity(
                id = 1102,
                lawId = 11,
                chapterId = 37,
                sectionNumberBn = "৭",
                sectionNumberEn = "7",
                titleBn = "তালাকের কার্যকারিতা ও নোটিশ প্রদান",
                titleEn = "Talaq notice and effectiveness",
                fullContentBn = """(১) যেকোনো পুরুষ তাহার স্ত্রীকে তালাক উচ্চারণ করিবার পর অবিলম্বে স্থানীয় ইউনিয়ন পরিষদ বা পৌরসভার চেয়ারম্যান/মেয়রকে লিখিত নোটিশ দান করিবেন এবং তাহার অনুলিপি স্ত্রীকে প্রেরণ করিবেন।
(২) চেয়ারম্যানের নিকট নোটিশ প্রাপ্তির তারিখ হইতে ৯০ (নব্বই) দিন অতিবাহিত না হওয়া পর্যন্ত তালাক কার্যকর হইবে না।
(৩) নোটিশ প্রাপ্তির ৩০ দিনের মধ্যে চেয়ারম্যান উভয় পক্ষের মধ্যে আপস নিষ্পত্তির জন্য একটি সালিসি পরিষদ গঠন করিবেন।""",
                fullContentEn = "Notice of Talaq must be sent to the Chairman and copy to wife; Talaq does not become effective until ninety days have expired from the day notice is received.",
                simplifiedExplanationBn = "মৌখিকভাবে তালাক বললেই তালাক চূড়ান্ত হয় না। চেয়ারম্যান বরাবর সরকারি নোটিশ পাঠানো বাধ্যতামূলক এবং নোটিশ পৌঁছানোর পর ৯০ দিন পার না হওয়া পর্যন্ত তালাক আইনত কার্যকর হয় না। এই ৯০ দিনের মধ্যে যেকোনো সময় স্বামী তালাক প্রত্যাহার করতে পারেন।",
                userNotes = null,
                punishmentSummaryBn = "নোটিশ না দিলে তালাক অকার্যকর থাকে এবং ১ বছর পর্যন্ত কারাদণ্ড হতে পারে",
                bailableStatus = "পারিবারিক আইন",
                cognizableStatus = "দেওয়ানি/পারিবারিক এখতিয়ার",
                trialCourt = "পারিবারিক আদালত ও সালিসি পরিষদ",
                orderIndex = 2
            ),

            // ============================================
            // 9. LIMITATION ACT, 1908 (তামাদি আইন)
            // ============================================
            SectionEntity(
                id = 1001,
                lawId = 10,
                chapterId = 36,
                sectionNumberBn = "৩",
                sectionNumberEn = "3",
                titleBn = "তামাদি অতিবাহিত মোকদ্দমা খারিজ (Dismissal of suits instituted after period)",
                titleEn = "Dismissal of suit, etc., instituted after period of limitation",
                fullContentBn = "আইনের তফসিল অনুযায়ী নির্ধারিত তামাদির মেয়াদ উত্তীর্ণ হওয়ার পর দায়েরকৃত প্রত্যেকটি মোকদ্দমা, আপিল ও দরখাস্ত খারিজ হইবে, যদিও বিবাদী তামাদির প্রশ্ন উত্থাপন না করিয়া থাকে।",
                fullContentEn = "Subject to provisions, every suit instituted, appeal preferred, and application made after the prescribed period shall be dismissed, although limitation has not been set up as a defence.",
                simplifiedExplanationBn = "আদালতের কঠোর নীতি: নির্ধারিত সময়ের পর আদালতে আসলে অধিকার তামাদি বারে বাতিল হয়ে যায়। বিবাদী পক্ষ যদি আপত্তি নাও তোলে, তবুও বিজ্ঞ আদালত স্বপ্রণোদিত হয়ে মামলাটি সরাসরি খারিজ করে দিতে বাধ্য।",
                userNotes = null,
                punishmentSummaryBn = "দেরিতে দায়েরকৃত মোকদ্দমা আদালতে সরাসরি খারিজ",
                bailableStatus = "তামাদি বিধান",
                cognizableStatus = "আইনগত বাধা",
                trialCourt = "সকল বিচারিক আদালত",
                orderIndex = 1
            ),
            SectionEntity(
                id = 1002,
                lawId = 10,
                chapterId = 36,
                sectionNumberBn = "৫",
                sectionNumberEn = "5",
                titleBn = "কতিপয় ক্ষেত্রে নির্ধারিত মেয়াদের সম্প্রসারণ (বিলম্ব মার্জনা / Condonation of Delay)",
                titleEn = "Extension of period in certain cases (Condonation of delay)",
                fullContentBn = "কোনো আপিল বা রিভিশন বা রায়ের পর্যালোচনার দরখাস্ত বা অন্য কোনো দরখাস্ত নির্ধারিত তামাদির মেয়াদ অতিবাহিত হওয়ার পরও আদালত গ্রহণ করিতে পারিবেন, যদি আবেদনকারী আদালতকে সন্তুষ্ট করিতে পারেন যে নির্ধারিত সময়ের মধ্যে তাহা দায়ের না করিবার পেছনে তাহার উপযুক্ত ও যুক্তিসঙ্গত কারণ ছিল।",
                fullContentEn = "Any appeal or application for a review of judgment or for leave to appeal may be admitted after the prescribed period of limitation, when the appellant or applicant satisfies the Court that he had sufficient cause for not preferring the appeal.",
                simplifiedExplanationBn = "আইনের অন্যতম বহুল ব্যবহৃত ধারা—বিলম্ব মার্জনা (Condonation of Delay)। অসুস্থতা, নকল বিলম্বে পাওয়া, আইনজীবীর ভুল ইত্যাদি যুক্তিসঙ্গত কারণ প্রমাণ করা গেলে আদালত দেরিতে আপিল বা রিভিশন দায়ের করার অনুমতি দিয়ে বিলম্ব ক্ষমা করতে পারে। মনে রাখতে হবে, ৫ ধারা মূল দেওয়ানি মোকদ্দমার ক্ষেত্রে প্রযোজ্য নয়।",
                userNotes = null,
                punishmentSummaryBn = "যথাযথ কারণ দর্শালে আদালত বিলম্ব ক্ষমা করে আপিল গ্রহণ করতে পারে",
                bailableStatus = "আইনি শিথিলতা",
                cognizableStatus = "আদালতের বিবেচনা",
                trialCourt = "আপিল ও রিভিশন আদালত",
                orderIndex = 2
            ),

            // ============================================
            // 10. NARCOTICS CONTROL ACT, 2018 (মাদকদ্রব্য নিয়ন্ত্রণ আইন)
            // ============================================
            SectionEntity(
                id = 801,
                lawId = 8,
                chapterId = 34,
                sectionNumberBn = "৩৬",
                sectionNumberEn = "36",
                titleBn = "মাদকদ্রব্য অপরাধের সারণি ও দণ্ড (ইয়াবা, হেরোইন, আইস ইত্যাদি)",
                titleEn = "Penalties for offences relating to narcotics (Schedule Table)",
                fullContentBn = """মাদকদ্রব্য নিয়ন্ত্রণ আইনের ৩৬ ধারার সারণি অনুযায়ী মাদকের পরিমাণ অনুযায়ী শাস্তি নির্ধারিত:
১. হেরোইন, কোকেন ও আইস: ২৫ গ্রামের অধিক হইলে মৃত্যুদণ্ড বা যাবজ্জীবন কারাদণ্ড এবং অন্যূন এক লক্ষ টাকা অর্থদণ্ড।
২. ইয়াবা বা এমফিটামিনযুক্ত ট্যাবলেট: ২০০ গ্রামের বেশি বা ৫০০ পিসের বেশি হইলে অনধিক যাবজ্জীবন কারাদণ্ড বা মৃত্যুদণ্ড।
৩. গাঁজা: ৫ কেজির অধিক হইলে পাঁচ হইতে দশ বৎসর কারাদণ্ড।""",
                fullContentEn = "Categorized penalties for drug offences: death penalty or life imprisonment for commercial quantities of heroin, ice, and amphetamines.",
                simplifiedExplanationBn = "মাদক কারবার রোধে কঠোর আইন। মাদকের ধরনের ওপর ভিত্তি করে সাজা নিশ্চিত করা হয়েছে। বাণিজ্যিক পরিমাণ বা বিপুল পরিমাণ ইয়াবা বা হেরোইন পাওয়া গেলে সর্বোচ্চ সাজা মৃত্যুদণ্ড বা যাবজ্জীবন কারাদণ্ড এবং জামিন পাওয়া প্রায় অসম্ভব।",
                userNotes = null,
                punishmentSummaryBn = "সর্বোচ্চ মৃত্যুদণ্ড বা যাবজ্জীবন সশ্রম কারাদণ্ড ও জরিমানা",
                bailableStatus = "অ-জামিনযোগ্য (Non-Bailable)",
                cognizableStatus = "আমলযোগ্য (Cognizable)",
                trialCourt = "বিশেষ দায়রা আদালত / মাদক ট্রাইব্যুনাল",
                orderIndex = 1
            )
        )

        return rawSections.map { sec ->
            sec.copy(
                contentHash = HashUtils.sha256(sec.fullContentBn),
                sourceUrl = lawSourceMap[sec.lawId] ?: "https://bdlaws.minlaw.gov.bd",
                extractionStatus = ExtractionStatus.COMPLETE,
                verificationStatus = VerificationStatus.UNVERIFIED,
                isRepealed = LegalTextParser.isRepealedOrOmitted(sec.titleBn, sec.fullContentBn),
                isAmended = LegalTextParser.isAmended(sec.titleBn, sec.fullContentBn)
            )
        }
    }

    fun extractSubsectionsForSections(sections: List<SectionEntity>): List<SubsectionEntity> {
        val result = mutableListOf<SubsectionEntity>()
        var syntheticId = 1L
        for (sec in sections) {
            val parsed = LegalTextParser.parseHierarchy(sec.fullContentBn)
            val tempToRealId = mutableMapOf<Int, Long>()
            for (node in parsed) {
                val realId = syntheticId++
                tempToRealId[node.tempId] = realId
                val parentRealId = node.parentTempId?.let { tempToRealId[it] }
                result.add(
                    SubsectionEntity(
                        id = realId,
                        sectionId = sec.id,
                        parentSubsectionId = parentRealId,
                        subsectionNumber = node.subsectionNumber,
                        nodeType = node.nodeType,
                        subsectionText = node.subsectionText,
                        sequenceOrder = node.sequenceOrder
                    )
                )
            }
        }
        return result
    }
}
