/**
 * TermIt Copyright (C) 2019 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.termit.environment;

import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.ontodriver.rdf4j.config.Rdf4jOntoDriverProperties;
import cz.cvut.kbss.termit.persistence.MainPersistenceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.*;

@Configuration
@EnableConfigurationProperties(cz.cvut.kbss.termit.util.Configuration.class)
@Profile("test")
public class TestPersistenceFactory {

    private final cz.cvut.kbss.termit.util.Configuration config;

    private EntityManagerFactory emf;

    @Autowired
    public TestPersistenceFactory(cz.cvut.kbss.termit.util.Configuration config) {
        this.config = config;
    }

    @Bean
    @Primary
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = MainPersistenceFactory.defaultParams();
        properties.put(ONTOLOGY_PHYSICAL_URI_KEY, config.getRepository().getUrl());
        properties.put(Rdf4jOntoDriverProperties.USE_VOLATILE_STORAGE, Boolean.TRUE.toString());
        properties.put(Rdf4jOntoDriverProperties.USE_INFERENCE, Boolean.TRUE.toString());
        properties.put(DATA_SOURCE_CLASS, config.getPersistence().getDriver());
        properties.put(LANG, config.getPersistence().getLanguage());
        properties.put(PREFER_MULTILINGUAL_STRING, Boolean.TRUE.toString());
        // OPTIMIZATION: Always use statement retrieval with unbound property. Should spare repository queries
        properties.put(Rdf4jOntoDriverProperties.LOAD_ALL_THRESHOLD, "1");
        properties.put(Rdf4jOntoDriverProperties.REPOSITORY_CONFIG, "classpath:rdf4j-memory-spin-rdfs.ttl");
        properties.put(Rdf4jOntoDriverProperties.INFERENCE_IN_DEFAULT_CONTEXT, Boolean.TRUE.toString());
        this.emf = Persistence.createEntityManagerFactory("termitTestPU", properties);
    }

    @PreDestroy
    private void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
