/*
 * Copyright 2022 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;

/**
 * @author Michael Reiche
 */
@SpringBootApplication
@RestController
@RequestMapping("")
public class DemoApplication {

	static String endpoint = "cb.daniel-wucmmh.sdk.cloud.couchbase.com"; // "cb.zsibzkbgllfbcj8g.cloud.couchbase.com";
	static String bucketName = "daniel-wucmmh";
	static String username = "qqulmR419fhfiEHFABFvinSXHzJRRjoU";
	static String password = "o@cpPMlRjswkuwKNsqzJHXK2v4HJaKeeu%a1ZCI%750bKDuXfcf9NMo!2cABNqCe";
  static String scopeName = "samples";
	static boolean tlsEnabled = true;


	@Bean
	Bucket getBucket(Cluster cluster){
		return cluster.bucket(bucketName);
	}

  @Bean
  Scope getScope(Bucket bucket) {
    return bucket.scope(scopeName);
  }
	@Bean
	Cluster getCluster(){
		ClusterEnvironment env = ClusterEnvironment.builder()
			.securityConfig(sc -> sc.enableTls(tlsEnabled))
			.ioConfig(ioc -> ioc.enableDnsSrv(true))
			//.loggerConfig(l -> l.consoleLogLevel(Level.ALL))
			//.loggerConfig( l -> l.fallbackToConsole(true))
			//.loggerConfig( l ->l.disableSlf4J(true))
			.build();

		// Initialize the Connection
		return Cluster.connect(
			"couchbases://"+"cb.daniel-wucmmh.sdk.cloud.couchbase.com",
			ClusterOptions.clusterOptions(
				"qqulmR419fhfiEHFABFvinSXHzJRRjoU",
				"o@cpPMlRjswkuwKNsqzJHXK2v4HJaKeeu%a1ZCI%750bKDuXfcf9NMo!2cABNqCe")
				.environment(env));
	}

	public static void main(String[] args) {
 		SpringApplication.run( DemoApplication.class, args );
	}


	@GetMapping("/")
	public String aaa_index() {
    return "<a href=api/hotels>api/hotels</a><br>";
  }

}
