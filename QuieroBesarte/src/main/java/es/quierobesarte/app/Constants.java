/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package es.quierobesarte.app;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public final class Constants {

	public static final String[] IMAGES = new String[] {
			// Heavy images
			"https://lh6.googleusercontent.com/-55osAWw3x0Q/URquUtcFr5I/AAAAAAAAAbs/rWlj1RUKrYI/s1024/A%252520Photographer.jpg",

	};

    public static final String[] IMAGESBIG = new String[] {
            // Heavy images
            "https://lh6.googleusercontent.com/-55osAWw3x0Q/URquUtcFr5I/AAAAAAAAAbs/rWlj1RUKrYI/s1024/A%252520Photographer.jpg",
            // Wrong link
    };

	private Constants() {
	}

	public static class Config {
		public static final boolean DEVELOPER_MODE = false;
        public static final String VERSION = "1.0";
        public static final String URL = "http://quierobesarte.es.nt5.unoeuro-server.com/";
	}
	
	public static class Extra {
		public static final String IMAGES = "es.quierobesarte.app.IMAGES";
		public static final String IMAGE_POSITION = "es.quierobesarte.app.IMAGE_POSITION";
        public static final String IMAGESBIG = "es.quierobesarte.app.IMAGESBIG";

    }
}
