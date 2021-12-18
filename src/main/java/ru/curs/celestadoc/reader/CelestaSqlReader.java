package ru.curs.celestadoc.reader;

import ru.curs.celesta.score.AbstractScore;
import ru.curs.celesta.score.Grain;
import ru.curs.celesta.score.ParseException;
import ru.curs.celesta.score.Score;
import ru.curs.celesta.score.discovery.ScoreByScorePathDiscovery;
import ru.curs.celesta.score.discovery.ScoreDiscovery;

import java.util.HashMap;
import java.util.Map;

public class CelestaSqlReader {
    private final AbstractScore abstractScore;

    public CelestaSqlReader(String path) throws ParseException {
        ScoreDiscovery discovery = new ScoreByScorePathDiscovery(path);
        abstractScore = new AbstractScore.ScoreBuilder<>(Score.class)
                .scoreDiscovery(discovery)
                .build();
    }

    public Map<String, Grain> getGrains() {
        Map<String, Grain> grains = new HashMap<>(abstractScore.getGrains());
        grains.remove(getSysSchemaName());

        return grains;
    }

    private String getSysSchemaName() {
        return abstractScore.getSysSchemaName();
    }
}
