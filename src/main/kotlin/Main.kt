import org.jsoup.Jsoup
import java.io.File
import java.lang.reflect.Field
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.deleteExisting

fun downloadFile(url: String, fileName: String) {
    val download = URL(url)
    download.openStream().use { Files.copy(it, Paths.get(fileName)) }
}

fun main(args: Array<String>) {
    val home =
        Jsoup.connect("https://clashnode.com/f/freenode")
            .timeout(0)
            .get()

    val list = home.getElementsByClass("post-list").get(0)
    val elem = list.getElementsByTag("li").get(0)
    val link = elem.getElementsByTag("a").get(0).attr("href")

    val nodes = Jsoup.connect(link).get()
    val ps = nodes.getElementsByTag("p")
    val clashnodes = ps.get(10).text()
    val currenttime = System.currentTimeMillis().toString()

    val currentPath = Paths.get(System.getProperty("user.dir"))
//    val filepath = currentPath.toString() +
//            "/clashnodes-" + currenttime + ".yaml"
    val filepath = currentPath.toString() + "/clashnodes.yaml"

    Path(filepath).deleteExisting()
    downloadFile(clashnodes, filepath)
}