@Grapes([
@Grab(group = 'net.minidev', module = 'json-smart', version = '2.0-RC2'),
@Grab(group = 'net.sourceforge.argo', module = 'argo', version = '3.3'),
@Grab(group = 'org.codehaus.jackson', module = 'jackson-mapper-asl', version = '1.9.12')
]
)
import fr.xebia.gpressure.Bench
import argo.jdom.JdomParser
import fr.xebia.gpressure.Test
import fr.xebia.gpressure.Validation
import groovy.json.JsonBuilder
import net.minidev.json.parser.JSONParser
import org.codehaus.jackson.map.ObjectMapper


// Init

def smart = new JSONParser(JSONParser.MODE_PERMISSIVE)
def mapperObject = new ObjectMapper()
def argoParser = new JdomParser()

// Data

def builder = new JsonBuilder()
builder.people {
    nom 'Simpson'
    prenom 'Homer'
    addresse 'Evergreen'
    age 36
    enfants 'Bart', 'Lisa', 'Maggie'
    femme {
        nom 'Simpson'
        prenom 'Marge'
        age 35
    }
}
def moi = builder.toString()
builder.voiture {
    marque 'citroën'
    portes 5
    automatique false
}
def car = builder.toString()

println "data:\n$moi\n$car"

// Bench
new Bench().with {
    flow()
    vusers(1)
    warmup(5)
    duration(10)
    threads(9)
    iteration(1)
    collectorInterval(10)
    sampleInterval(5)

    tests(
            { o, d -> return mapperObject.readTree(d[0] as String) } as Test,
            { o, d -> return smart.parse(d[0] as String) } as Test,
            { o, d -> return argoParser.parse(d[0] as String) } as Test,
    )
    //validate({ d, r -> r != null } as Validation)
    detailedCollector()
    data([moi], [car])
    start()
}