/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.examples.demo;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;
import static java.nio.charset.StandardCharsets.UTF_8;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.tls.HandshakeCertificates;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.couchbase.client.core.env.IoConfig;
import com.couchbase.client.core.env.SecurityConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ClusterOptions;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.manager.query.CreatePrimaryQueryIndexOptions;
import com.couchbase.client.java.query.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sample code for connecting to Capella through the data-plane. <br>
 * 1) Create a cluster that has data, index and query nodes. <br>
 * 2) Cluster -> Connectivity : allow your client ip address (or all ip address 0/0.0.0.0)<br>
 * 3) Create a user "user" in the cluster with password "Couch0base!" and Read/Write access to all buckets <br>
 * 4) Create a bucket named "my_bucket" <br>
 * 5) Get your access key from API Keys. The secret key is available only when the key is generated. If you have not
 * saved it, then generate a new key and save the secret key. <br>
 */
public class ConnectSample {

	// Update this to your cluster
	static String endpoint = null; // "cb.zsibzkbgllfbcj8g.cloud.couchbase.com";
	static String bucketName = "my_bucket";
	static String username = "user";
	static String password = "Couch0base!";
  // User Input ends here.

	static boolean tlsEnabled = true;
	/* to connect to a local couchbase server
  static String endpoint = "localhost";
	static String bucketName = "my_bucket";
	static String username = "Administrator";
	static String password = "password";
	static boolean tlsEnabled = false;
  */

	public static void main(String... args) {

		ClusterEnvironment env = ClusterEnvironment.builder()
				.securityConfig(sc -> sc.enableTls(tlsEnabled))
				.ioConfig(ioc -> ioc.enableDnsSrv(true)).build();

		// Initialize the Connection
		Cluster cluster = Cluster.connect(endpoint, ClusterOptions.clusterOptions(username, password).environment(env));
		Bucket bucket = cluster.bucket(bucketName);
		bucket.waitUntilReady(Duration.parse("PT10S"));
		Collection collection = bucket.defaultCollection();

		cluster.queryIndexes().createPrimaryIndex(bucketName,
				CreatePrimaryQueryIndexOptions.createPrimaryQueryIndexOptions().ignoreIfExists(true));

		// Create a JSON Document
		JsonObject arthur = JsonObject.create().put("name", "Arthur").put("email", "kingarthur@couchbase.com")
				.put("interests", JsonArray.from("Holy Grail", "African Swallows"));

		// Store the Document
		collection.upsert("u:king_arthur", arthur);

		// Load the Document and print it
		// Prints Content and Metadata of the stored Document
		System.err.println(collection.get("u:king_arthur"));

		// Perform a N1QL Query
		QueryResult result = cluster.query(String.format("SELECT name FROM `%s` WHERE $1 IN interests", bucketName),
				queryOptions().parameters(JsonArray.from("African Swallows")));

		// Print each found Row
		for (JsonObject row : result.rowsAsObject()) {
			System.err.println(row);
		}

		cluster.disconnect();
	}
}
