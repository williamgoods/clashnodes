import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import java.io.File
import java.lang.StringBuilder
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.ArrayList
import kotlin.io.path.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists

@Serializable
data class Times (
    var time: List<String>
)

fun downloadFile(url: String, fileName: String) {
    val download = URL(url)
    download.openStream().use { Files.copy(it, Paths.get(fileName)) }
}

fun main(args: Array<String>) {
    val currentPath = Paths.get(System.getProperty("user.dir")).toString()

    val home =
        Jsoup.connect("https://clashnode.com/f/freenode")
            .timeout(0)
            .get()

    val list = home.getElementsByClass("post-list").get(0)
    val elem = list.getElementsByTag("li").get(0)
    val link = elem.getElementsByTag("a").get(0).attr("href")
    val time = elem.getElementsByTag("a").get(2).text()
    val regex = Regex("""\d*月\d*日""")
    val currenttime =
        regex.find(time)?.value?.
                replace("月", "-")?.
                replace("日", "")
    println("current time: $currenttime")

    val timefile = currentPath + "/currenttime.yaml"
    val parsedYaml =
        Yaml.default.decodeFromString(Times.serializer(),
            Path.of(timefile).toFile().readText())
    val lasttime = parsedYaml.time[0]

    if (lasttime != currenttime) {
        val clashnodespath = currentPath + "/clashnodes.yaml"

        val yesterdayclashnodepath =
                        currentPath + "/clashnodes-" + lasttime + ".yaml"
        if (!File(yesterdayclashnodepath).exists()){
            val success =
                File(clashnodespath).renameTo(File(yesterdayclashnodepath))

            if (success) {
                println("clashnodes backup succeeded!")
                val timelist = ArrayList<String>()
                timelist.add(currenttime.toString())
                parsedYaml.time = timelist

                val result =
                    Yaml.default.encodeToString(Times.serializer(), parsedYaml)
                println(result)
                File(timefile).writeText(result)

                val nodes = Jsoup.connect(link).get()
                val ps = nodes.getElementsByTag("p")
                val clashnodes = ps.get(10).text()

                Path(clashnodespath).deleteIfExists()
                downloadFile(clashnodes, clashnodespath)
            }
        }
    }

//    val currenttime = System.currentTimeMillis().toString()

//    val filepath = currentPath.toString() +
//            "/clashnodes-" + currenttime + ".yaml"
}