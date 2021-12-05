package wtf.jaren.aero.velocity.plan

import com.djrapitops.plan.query.QueryService
import java.util.*

class PlanAPI {
    fun getPlaytime(player: UUID): Long {
        var playtime = 0L
        val servers = QueryService.getInstance().commonQueries
            .fetchServerUUIDs()
        for (server in servers) {
            if (server == UUID.fromString("43a66403-7a22-41ae-ae92-d10b91157c2c")) continue
            playtime += QueryService.getInstance().commonQueries.fetchPlaytime(
                player,
                server,
                0,
                System.currentTimeMillis()
            )
        }
        return playtime
    }
}