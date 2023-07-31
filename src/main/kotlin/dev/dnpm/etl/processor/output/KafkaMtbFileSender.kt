/*
 * This file is part of ETL-Processor
 *
 * Copyright (c) 2023  Comprehensive Cancer Center Mainfranken, Datenintegrationszentrum Philipps-Universität Marburg and Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.dnpm.etl.processor.output

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate

class KafkaMtbFileSender(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : MtbFileSender {

    private val logger = LoggerFactory.getLogger(KafkaMtbFileSender::class.java)

    override fun send(request: MtbFileSender.MtbFileRequest): MtbFileSender.Response {
        return try {
            val result = kafkaTemplate.sendDefault(
                header(request),
                objectMapper.writeValueAsString(request.mtbFile)
            )
            if (result.get() != null) {
                logger.debug("Sent file via KafkaMtbFileSender")
                MtbFileSender.Response(MtbFileSender.ResponseStatus.SUCCESS)
            } else {
                MtbFileSender.Response(MtbFileSender.ResponseStatus.ERROR)
            }

        } catch (e: Exception) {
            logger.error("An error occurred sending to kafka", e)
            MtbFileSender.Response(MtbFileSender.ResponseStatus.UNKNOWN)
        }
    }

    // TODO not yet implemented
    override fun send(request: MtbFileSender.DeleteRequest): MtbFileSender.Response {
        return MtbFileSender.Response(MtbFileSender.ResponseStatus.UNKNOWN)
    }

    private fun header(request: MtbFileSender.MtbFileRequest): String {
        return "{\"pid\": \"${request.mtbFile.patient.id}\", " +
                "\"eid\": \"${request.mtbFile.episode.id}\", " +
                "\"requestId\": \"${request.requestId}\"}"
    }
}