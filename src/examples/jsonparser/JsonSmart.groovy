
@Grapes(
@Grab(group = 'net.minidev', module = 'json-smart', version = '2.0-RC2')
)
import fr.xebia.microbench.Test
import fr.xebia.microbench.Bench

new Bench().with {
    tests({ } as Test)
}
