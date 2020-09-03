package eu.ecoepi.iris.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import eu.ecoepi.iris.LifeCycleStage;
import eu.ecoepi.iris.Parameters;
import eu.ecoepi.iris.Randomness;
import eu.ecoepi.iris.SpatialIndex;
import eu.ecoepi.iris.components.Position;
import eu.ecoepi.iris.components.TickAbundance;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

@All({TickAbundance.class, Position.class})
public class Dispersal extends IteratingSystem {

    ComponentMapper<TickAbundance> abundanceMapper;
    ComponentMapper<Position> positionMapper;

    final EnumeratedDistribution<Integer> distribution;

    @Wire
    SpatialIndex index;

    @Wire
    Randomness randomness;

    public Dispersal() {
        final List<Pair<Integer, Double>> distanceProbabilities = new ArrayList<>();

        distanceProbabilities.add(new Pair<>(1, 0.25));
        distanceProbabilities.add(new Pair<>(2, 0.25));
        distanceProbabilities.add(new Pair<>(3, 0.20));
        distanceProbabilities.add(new Pair<>(4, 0.15));
        distanceProbabilities.add(new Pair<>(5, 0.05));
        distanceProbabilities.add(new Pair<>(6, 0.04));
        distanceProbabilities.add(new Pair<>(7, 0.03));
        distanceProbabilities.add(new Pair<>(8, 0.02));
        distanceProbabilities.add(new Pair<>(9, 0.01));

        for (int i = 0, n = distanceProbabilities.size(); i < n; ++i) {
            var distance = distanceProbabilities.get(i);
            distanceProbabilities.add(new Pair<>(-distance.getFirst(), distance.getSecond()));
        }

        distribution = new EnumeratedDistribution<>(distanceProbabilities);
    }

    @Override
    protected void process(int entityId) {
        var abundance = abundanceMapper.get(entityId);
        var position = positionMapper.get(entityId);

        while (true) {
            var x = distribution.sample();
            var y = distribution.sample();

            var neighbourToRandom = index.lookUp(position.moveBy(x, y));
            if (neighbourToRandom.isPresent()) {
                var abundanceToRandom = abundanceMapper.get(neighbourToRandom.get());
                var movingLarvae = randomness.roundRandom(abundance.getStage(LifeCycleStage.LARVAE) * Parameters.DISPERSAL_RATE.get(LifeCycleStage.LARVAE));
                abundance.addLarvae(-movingLarvae);
                abundanceToRandom.addFedLarvae(movingLarvae);
                break;
            }
        }

        while (true) {
            var x = distribution.sample();
            var y = distribution.sample();

            var neighbourToRandom = index.lookUp(position.moveBy(x, y));
            if (neighbourToRandom.isPresent()) {
                var abundanceToRandom = abundanceMapper.get(neighbourToRandom.get());
                var movingNymphs = randomness.roundRandom(abundance.getStage(LifeCycleStage.NYMPH) * Parameters.DISPERSAL_RATE.get(LifeCycleStage.NYMPH));
                abundance.addNymphs(-movingNymphs);
                abundanceToRandom.addFedNymphs(movingNymphs);
                break;
            }
        }

        while (true) {
            var x = distribution.sample();
            var y = distribution.sample();

            var neighbourToRandom = index.lookUp(position.moveBy(x, y));
            if (neighbourToRandom.isPresent()) {
                var abundanceToRandom = abundanceMapper.get(neighbourToRandom.get());
                var movingAdults = randomness.roundRandom(abundance.getStage(LifeCycleStage.ADULT) * Parameters.DISPERSAL_RATE.get(LifeCycleStage.ADULT));
                abundance.addAdults(-movingAdults);
                abundanceToRandom.addFedAdults(movingAdults);
                break;
            }
        }
    }

}
