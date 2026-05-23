package com.astranavi.app.ui.knowledge

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.astranavi.app.LocalTopBarTitle
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astranavi.app.R
import com.astranavi.app.ui.components.responsiveGridCells
import com.astranavi.app.ui.components.responsiveMetrics
import com.astranavi.app.ui.components.titleCase
import com.astranavi.app.util.currentAppLocale

data class Yoga(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val sanskrit: String,
    val represents: String,
    val planet: String,
    val condition: String,
    val results: String,
    val deepDive: String,
    val classification: String,
    val color: Color,
    val icon: ImageVector
)

val yogas = listOf(
    Yoga(
        id = "raja",
        nameEn = "Raja Yoga",
        nameHi = "राज योग",
        sanskrit = "Raja Yoga",
        represents = "Power, authority, royal status, success in high positions, leadership.",
        planet = "9th & 10th Lords",
        condition = "Union of Kendra (Action) & Trikona (Luck)",
        results = "Highest worldly success and social status.",
        deepDive = "Raja Yoga is the ultimate combination for worldly success. It is formed when the lords of the angular houses (Kendra) meet the lords of the trinal houses (Trikona). This union blends effort with divine grace, leading to an elevated social position.",
        classification = "Kendra-Trikona Union",
        color = Color(0xFFF59E0B),
        icon = Icons.Default.Star
    ),
    Yoga(
        id = "dhana",
        nameEn = "Dhana Yoga",
        nameHi = "धन योग",
        sanskrit = "Dhana Yoga",
        represents = "Wealth accumulation, financial stability, material success, abundance.",
        planet = "2nd & 11th Lords",
        condition = "Connection between Earnings (2nd) & Savings (11th)",
        results = "Strong capacity for earnings and assets.",
        deepDive = "Dhana Yoga focuses purely on material and financial gains. It occurs when the planets ruling the 2nd (wealth) and 11th (gains) houses form a relationship with the 5th or 9th houses.",
        classification = "Wealth Union",
        color = Color(0xFF10B981),
        icon = Icons.Default.MonetizationOn
    ),
    Yoga(
        id = "gajakesari",
        nameEn = "Gajakesari",
        nameHi = "गजकेसरी योग",
        sanskrit = "Gajakesari Yoga",
        represents = "Wisdom, lasting reputation, eloquence, victory over enemies.",
        planet = "Jupiter & Moon",
        condition = "Jupiter in Kendra from Moon",
        results = "Wisdom, virtue, and long-lasting fame.",
        deepDive = "Represented by the Elephant (Gaja) and the Lion (Kesari), this yoga brings together the wisdom of Jupiter and the mental strength of the Moon. It makes the native deeply respected and often leads to an abundance of intellectual brilliance.",
        classification = "Jup-Moon Relation",
        color = Color(0xFF3B82F6),
        icon = Icons.Default.AutoAwesome
    ),
    Yoga(
        id = "mahapurusha",
        nameEn = "Mahapurusha",
        nameHi = "महापुरुष योग",
        sanskrit = "Pancha Mahapurusha",
        represents = "Extraordinary personality, specialized greatness, mastery in a field.",
        planet = "Non-Luminaries",
        condition = "Planet in Own/Exalt Sign in Kendra",
        results = "Exceptional human qualities based on the ruling planet.",
        deepDive = "These five 'Great Person' yogas are formed by Mars (Ruchaka), Mercury (Bhadra), Jupiter (Hamsa), Venus (Malavya), and Saturn (Shasha). They create an individual who embodies the highest virtues of that planet.",
        classification = "Planetary Strength",
        color = Color(0xFF8B5CF6),
        icon = Icons.AutoMirrored.Filled.TrendingUp
    ),
    Yoga(
        id = "viparita",
        nameEn = "Viparita Raja",
        nameHi = "विपरीत राज योग",
        sanskrit = "Viparita Raja Yoga",
        represents = "Success after crisis, power through reversal, benefit from loss of others.",
        planet = "6th, 8th, 12th Lords",
        condition = "Negative Planets in Negative Houses",
        results = "Resilience and breakthrough during times of crisis.",
        deepDive = "A highly strategic yoga where the lords of the 'difficult houses' are placed within other difficult houses. It signifies that the native will rise to power by overcoming immense obstacles.",
        classification = "Dusthana Alchemy",
        color = Color(0xFFF97316),
        icon = Icons.Default.FlashOn
    )
)

val yogasHi: List<Yoga> = listOf(
    Yoga(
        id = "raja",
        nameEn = "Raja Yoga",
        nameHi = "राज योग",
        sanskrit = "Raja Yoga",
        represents = "शक्ति, सत्ता, राजकीय स्थिति, उच्च पदों में सफलता, नेतृत्व।",
        planet = "नवें और दसवें भाव के स्वामी",
        condition = "केन्द्र (कार्य) और त्रिकोण (भाग्य) का मेल",
        results = "सर्वोच्च सांसारिक सफलता और सामाजिक प्रतिष्ठा।",
        deepDive = "राज योग सांसारिक सफलता का सर्वोत्तम संयोग है। यह तब बनता है जब कोणीय भावों (केन्द्र) के स्वामी त्रिकोणीय भावों (त्रिकोण) के स्वामियों से मिलते हैं। यह संयोग प्रयास को दिव्य कृपा के साथ मिलाता है, जिससे ऊंचा सामाजिक स्थान मिलता है।",
        classification = "केन्द्र-त्रिकोण संयोग",
        color = Color(0xFFF59E0B),
        icon = Icons.Default.Star
    ),
    Yoga(
        id = "dhana",
        nameEn = "Dhana Yoga",
        nameHi = "धन योग",
        sanskrit = "Dhana Yoga",
        represents = "धन संचय, आर्थिक स्थिरता, भौतिक सफलता, समृद्धि।",
        planet = "दूसरे और ग्यारहवें भाव के स्वामी",
        condition = "आय (दूसरा भाव) और बचत (ग्यारहवां भाव) का संबंध",
        results = "आय और संपत्ति की मजबूत क्षमता।",
        deepDive = "धन योग पूरी तरह से भौतिक और आर्थिक लाभ पर केंद्रित है। यह तब बनता है जब दूसरे (धन) और ग्यारहवें (लाभ) भावों के ग्रह पांचवें या नवें भावों से संबंध बनाते हैं।",
        classification = "धन संयोग",
        color = Color(0xFF10B981),
        icon = Icons.Default.MonetizationOn
    ),
    Yoga(
        id = "gajakesari",
        nameEn = "Gajakesari",
        nameHi = "गजकेसरी योग",
        sanskrit = "Gajakesari Yoga",
        represents = "ज्ञान, स्थायी प्रतिष्ठा, वाक्पटुता, शत्रुओं पर जीत।",
        planet = "गुरु और चंद्रमा",
        condition = "चंद्रमा से केन्द्र में गुरु",
        results = "ज्ञान, गुण और दीर्घस्थायी यश।",
        deepDive = "हाथी (गज) और शेर (केसरी) के प्रतीक के रूप में, यह योग गुरु की बुद्धिमत्ता और चंद्रमा की मानसिक शक्ति को एकत्रित करता है। यह देशज को गहराई से सम्मानित करता है और अक्सर बौद्धिक प्रतिभा की प्रचुरता की ओर ले जाता है।",
        classification = "गुरु-चंद्र संबंध",
        color = Color(0xFF3B82F6),
        icon = Icons.Default.AutoAwesome
    ),
    Yoga(
        id = "mahapurusha",
        nameEn = "Mahapurusha",
        nameHi = "महापुरुष योग",
        sanskrit = "Pancha Mahapurusha",
        represents = "असाधारण व्यक्तित्व, विशेष महानता, किसी क्षेत्र में कुशलता।",
        planet = "गैर-ज्योति ग्रह",
        condition = "अपने/उच्च राशि में केन्द्र में ग्रह",
        results = "शासक ग्रह के आधार पर असाधारण मानवीय गुण।",
        deepDive = "ये पांच 'महान व्यक्ति' योग मंगल (रुचक), बुध (भद्र), गुरु (हंस), शुक्र (मालव्य) और शनि (शष) द्वारा बनते हैं। ये एक ऐसे व्यक्ति को बनाते हैं जो उस ग्रह के सर्वोच्च गुणों को प्रदर्शित करता है।",
        classification = "ग्रह शक्ति",
        color = Color(0xFF8B5CF6),
        icon = Icons.AutoMirrored.Filled.TrendingUp
    ),
    Yoga(
        id = "viparita",
        nameEn = "Viparita Raja",
        nameHi = "विपरीत राज योग",
        sanskrit = "Viparita Raja Yoga",
        represents = "संकट के बाद सफलता, उलटफेर के माध्यम से शक्ति, दूसरों के नुकसान से लाभ।",
        planet = "छठे, आठवें, बारहवें भाव के स्वामी",
        condition = "नकारात्मक ग्रह नकारात्मक भावों में",
        results = "संकट के समय में लचीलापन और सफलता।",
        deepDive = "एक अत्यंत रणनीतिक योग जहां 'कठिन भावों' के स्वामी अन्य कठिन भावों में रखे जाते हैं। यह इंगित करता है कि जातक विशाल बाधाओं को दूर करके सत्ता तक पहुंचेगा।",
        classification = "दुष्ठान संयोग",
        color = Color(0xFFF97316),
        icon = Icons.Default.FlashOn
    )
)

val yogasKo: List<Yoga> = listOf(
    Yoga(
        id = "raja",
        nameEn = "Raja Yoga",
        nameHi = "राज योग",
        sanskrit = "Raja Yoga",
        represents = "권력, 권위, 왕실 지위, 높은 직위에서의 성공, 리더십.",
        planet = "9번째 및 10번째 주인",
        condition = "켄드라(행동)와 트리코나(행운)의 결합",
        results = "최고의 세상 성공과 사회적 지위.",
        deepDive = "라자 요가는 세상의 성공을 위한 최고의 조합입니다. 각진 집(켄드라)의 주인과 삼각형 집(트리코나)의 주인이 만날 때 형성됩니다. 이 결합은 노력과 신성한 은혜를 섞어서 높은 사회적 지위로 이어집니다.",
        classification = "켄드라-트리코나 결합",
        color = Color(0xFFF59E0B),
        icon = Icons.Default.Star
    ),
    Yoga(
        id = "dhana",
        nameEn = "Dhana Yoga",
        nameHi = "धन योग",
        sanskrit = "Dhana Yoga",
        represents = "부의 축적, 재정 안정, 물질적 성공, 풍요로움.",
        planet = "2번째 및 11번째 주인",
        condition = "소득(2번째)과 저축(11번째) 사이의 연결",
        results = "강한 수입 및 자산 능력.",
        deepDive = "다나 요가는 순전히 물질 및 금융 이득에 중점을 둡니다. 2번째(부) 및 11번째(이득) 집의 행성이 5번째 또는 9번째 집과 관계를 형성할 때 발생합니다.",
        classification = "부 결합",
        color = Color(0xFF10B981),
        icon = Icons.Default.MonetizationOn
    ),
    Yoga(
        id = "gajakesari",
        nameEn = "Gajakesari",
        nameHi = "गजकेसरी योग",
        sanskrit = "Gajakesari Yoga",
        represents = "지혜, 지속적인 명성, 웅변, 적에 대한 승리.",
        planet = "목성 및 달",
        condition = "달에서 켄드라의 목성",
        results = "지혜, 덕행 및 오래 지속되는 명성.",
        deepDive = "코끼리(가자)와 사자(케사리)로 표현되는 이 요가는 목성의 지혜와 달의 정신적 힘을 함께 가져옵니다. 그것은 태어난 사람을 깊이 존경받게 만들고 종종 지적 수월성의 풍요로움으로 이어집니다.",
        classification = "목성-달 관계",
        color = Color(0xFF3B82F6),
        icon = Icons.Default.AutoAwesome
    ),
    Yoga(
        id = "mahapurusha",
        nameEn = "Mahapurusha",
        nameHi = "महापुरुष योग",
        sanskrit = "Pancha Mahapurusha",
        represents = "비범한 성격, 전문화된 위대함, 분야의 숙달.",
        planet = "비발광 행성",
        condition = "켄드라의 자신의/고귀한 표시의 행성",
        results = "지배 행성을 기반으로 한 예외적 인간 품질.",
        deepDive = "이 다섯 가지 '위대한 사람' 요가는 화성(루차카), 수성(바드라), 목성(한사), 금성(말라비야) 및 토성(샤샤)에 의해 형성됩니다. 그들은 그 행성의 최고 미덕을 체화하는 개인을 만듭니다.",
        classification = "행성 힘",
        color = Color(0xFF8B5CF6),
        icon = Icons.AutoMirrored.Filled.TrendingUp
    ),
    Yoga(
        id = "viparita",
        nameEn = "Viparita Raja",
        nameHi = "विपरीत राज योग",
        sanskrit = "Viparita Raja Yoga",
        represents = "위기 후 성공, 반전을 통한 힘, 다른 사람의 손실로부터의 이득.",
        planet = "6번째, 8번째, 12번째 주인",
        condition = "부정적인 집에서 부정적 행성",
        results = "위기 시간에 회복력과 돌파구.",
        deepDive = "'어려운 집'의 주인이 다른 어려운 집 내에 위치하는 매우 전략적인 요가입니다. 그것은 태어난 사람이 엄청난 장애물을 극복함으로써 권력으로 상승할 것임을 나타냅니다.",
        classification = "두스타나 연금술",
        color = Color(0xFFF97316),
        icon = Icons.Default.FlashOn
    )
)

fun yogasFor(locale: String): List<Yoga> = when (locale) {
    "hi" -> yogasHi
    "ko" -> yogasKo
    else -> yogas
}

@Composable
fun YogaScreen(onBack: () -> Unit, onOpenDrawer: () -> Unit) {
    var selectedYoga by remember { mutableStateOf<Yoga?>(null) }
    val setTitle = LocalTopBarTitle.current
    val defaultTitle = stringResource(R.string.knowledge_title_yogas)
    val localizedYogas = yogasFor(currentAppLocale().language)

    LaunchedEffect(selectedYoga) {
        setTitle?.invoke(selectedYoga?.nameEn ?: defaultTitle)
    }

    BackHandler(enabled = true) {
        if (selectedYoga != null) {
            selectedYoga = null
        } else {
            onBack()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color.Transparent)) {
            if (selectedYoga == null) {
                YogaList(localizedYogas) { selectedYoga = it }
            } else {
                YogaDetail(selectedYoga!!)
            }
        }
    }
}

@Composable
fun YogaList(yogas: List<Yoga>, onSelect: (Yoga) -> Unit) {
    val metrics = responsiveMetrics()

    LazyVerticalGrid(
        columns = responsiveGridCells(),
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        contentPadding = PaddingValues(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(this.maxLineSpan) }) {
            Text(stringResource(R.string.knowledge_yoga_intro),
                 style = MaterialTheme.typography.bodyMedium,
                 color = MaterialTheme.colorScheme.onBackground,
                 fontWeight = FontWeight.Bold,
                 modifier = Modifier.padding(bottom = 8.dp))
        }
        
        items(yogas, key = { it.id }) { yoga ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(yoga) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(metrics.pagePadding), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(if (metrics.isCompactWidth) 48.dp else 56.dp).background(yoga.color, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(yoga.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(yoga.nameEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(yoga.classification, style = MaterialTheme.typography.labelSmall, color = yoga.color, fontWeight = FontWeight.Black)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun YogaDetail(yoga: Yoga) {
    val metrics = responsiveMetrics()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(metrics.pagePadding),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(2.dp, yoga.color)
        ) {
            Column(modifier = Modifier.padding(metrics.cardPadding * 1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(yoga.icon, contentDescription = null, tint = yoga.color, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(yoga.nameEn.titleCase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(yoga.nameHi, style = MaterialTheme.typography.titleMedium, color = yoga.color, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            YogaStatCard(stringResource(R.string.knowledge_yoga_stats_classification), yoga.classification, yoga.color, Modifier.weight(1f))
            YogaStatCard(stringResource(R.string.knowledge_yoga_stats_key_planets), yoga.planet, yoga.color, Modifier.weight(1f))
        }

        // Logic Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                Text(stringResource(R.string.knowledge_yoga_logic), style = MaterialTheme.typography.labelSmall, color = yoga.color, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(yoga.condition, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }

        // Deep Dive
        Text(stringResource(R.string.knowledge_yoga_detailed_analysis), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        Text(
            text = yoga.deepDive,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        // Results
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(metrics.cardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.knowledge_yoga_expected_results), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(yoga.results, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun YogaStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
