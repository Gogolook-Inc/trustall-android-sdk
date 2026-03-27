package com.gogolook.trustall.demo.feature.search

data class SampleNumber(
    val country: String,
    val category: String,
    val number: String
)

object SampleData {
    private val rawSamples = listOf(
        "[JP][beauty][+81270251881]",
        "[JP][government][+81989175334]",
        "[JP][life][+81172753300]",
        "[JP][media][+81832663502]",
        "[JP][organization][+81482412700]",
        "[JP][organization][+81339668481]",
        "[JP][others][+81791222287]",
        "[JP][professional][+81962740600]",
        "[JP][activity][+81454823888]",
        "[JP][][+818003008205]",

        "[BR][automobile][+551638261757]",
        "[BR][bank][+551934939620]",
        "[BR][politics][+552125279343]",
        "[BR][][+5561992564970]",

        "[HK][][+85237484165]",
        "[HK][][+85238515946]",
        "[HK][][+85260918716]",
        "[HK][][+85231116777]",
        "[HK][][+85221543736]",
        "[HK][][+85237091877]",
        "[HK][][+85229451091]",

        "[KR][education][+82316139406]",
        "[KR][][+821023485055]",

        "[MY][][+6089224771]",

        "[PH][publicperson][+639171647956]",

        "[TH][pet][+66992499163]",
        "[TH][travel][+66909829787]",
        "[TH][][+66632181065]",
        "[TH][][+66657398286]",
        "[TH][][+66966970386]",

        "[TW][entertainment][+88635742814]",
        "[TW][food][+886229116798]",
        "[TW][health][+88638579760]",
        "[TW][health][+886289417376]",
        "[TW][logistic][+886977224577]",
        "[TW][shopping][+886227556047]",
        "[TW][traffic][+886425663400]",
        "[TW][][+886937709688]",
        "[TW][][+886976825935]"
    )

    val samples: List<SampleNumber> by lazy {
        val regex = Regex("\\[([A-Z]+)\\]\\[(.*?)\\]\\[(\\+?\\d+)\\]")
        rawSamples.mapNotNull { raw ->
            val match = regex.find(raw)
            if (match != null) {
                val (country, category, number) = match.destructured
                SampleNumber(country, category, number)
            } else {
                null
            }
        }
    }
    
    val countries: List<String> by lazy {
        samples.map { it.country }.distinct().sorted()
    }
}
