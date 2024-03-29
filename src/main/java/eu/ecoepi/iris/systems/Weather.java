package eu.ecoepi.iris.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import eu.ecoepi.iris.resources.Parameters;
import eu.ecoepi.iris.components.*;
import eu.ecoepi.iris.resources.TimeStep;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@All({Habitat.class})
public class Weather extends IteratingSystem {

    ComponentMapper<Temperature> temperatureMapper;
    ComponentMapper<Humidity> humidityMapper;
    ComponentMapper<Habitat> habitatMapper;

    final List<Float> meanTempTimeSeries = new ArrayList<>();
    final List<Float> minTempTimeSeries = new ArrayList<>();
    final List<Float> maxTempTimeSeries = new ArrayList<>();
    final List<Float> humidityTimeSeries = new ArrayList<>();

    @Wire
    TimeStep timestep;

    public Weather(String path) throws IOException, CsvException {
        CSVReader reader = new CSVReaderBuilder(new FileReader(path))
                .withSkipLines(1)
                .build();
        String[] nextLine;

        while ((nextLine = reader.readNext()) != null) {
            meanTempTimeSeries.add(Float.parseFloat(nextLine[0]));
            minTempTimeSeries.add(Float.parseFloat(nextLine[1]));
            maxTempTimeSeries.add(Float.parseFloat(nextLine[2]));
            humidityTimeSeries.add(Float.parseFloat(nextLine[3]));
        }
    }

    @Override
    protected void process(int entityId) {
        var temperature = temperatureMapper.get(entityId);
        var humidity = humidityMapper.get(entityId);
        var habitat = habitatMapper.get(entityId);

        var currentTimeStep = timestep.getCurrent();
        var adjustedMeanTemperature = 0f;
        var adjustedMinTemperature = 0f;
        var adjustedMaxTemperature = 0f;

        if ((currentTimeStep > Parameters.BEGIN_SPRING && currentTimeStep <= Parameters.BEGIN_SUMMER) ||
                (currentTimeStep > Parameters.BEGIN_AUTUMN && currentTimeStep < Parameters.BEGIN_WINTER)) { // Spring or autumn

            var microTempSpringAutumn = Parameters.LOCAL_CLIMATE_SPRING_AUTUMN.get(habitat.getType());
            adjustedMeanTemperature = microTempSpringAutumn;
            adjustedMaxTemperature = microTempSpringAutumn;

        } else if (currentTimeStep > Parameters.BEGIN_SUMMER && currentTimeStep <= Parameters.BEGIN_AUTUMN) { // Summer
            var microTempSummer = Parameters.LOCAL_CLIMATE_SUMMER.get(habitat.getType());
            adjustedMeanTemperature = microTempSummer;
            adjustedMaxTemperature = microTempSummer;

        }

        temperature.setMeanTemperature(meanTempTimeSeries.get(currentTimeStep) + adjustedMeanTemperature);
        temperature.setMinTemperature(minTempTimeSeries.get(currentTimeStep) + adjustedMinTemperature);
        temperature.setMaxTemperature(maxTempTimeSeries.get(currentTimeStep) + adjustedMaxTemperature);

        var adjustedRelativeHumidity = humidityTimeSeries.get(currentTimeStep) * Parameters.LOCAL_HUMIDITY.get(habitat.getType());
        if (adjustedRelativeHumidity > 100) {
            adjustedRelativeHumidity = 100;
        }
        humidity.setRelativeHumidity(adjustedRelativeHumidity);
    }
}
