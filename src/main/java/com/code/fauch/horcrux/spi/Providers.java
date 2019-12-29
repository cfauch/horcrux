/*
 * Copyright 2019 Claire Fauch
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.code.fauch.horcrux.spi;

import java.util.ServiceLoader;

/**
 * Utility class to make easier the research of horcrux providers using SPI.
 * 
 * @author c.fauch
 *
 */
public final class Providers {

    /**
     * The service loader to use to research horcrux providers.
     */
    private static final ServiceLoader<IHorcrux> HX = ServiceLoader.load(IHorcrux.class);

    /**
     * Research a horcrux of the given name.
     * 
     * @param name the name of the horcrux to return (not null)
     * @return the corresponding horcrux instance
     */
    public static IHorcrux getHorcruxInstance(final String name) {
        for (IHorcrux hx : HX) {
            if (hx.accept(name)) {
                return hx;
            }
        }
        return null;
    }
    
}
