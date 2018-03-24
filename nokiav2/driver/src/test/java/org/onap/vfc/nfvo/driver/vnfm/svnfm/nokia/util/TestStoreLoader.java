/*
 * Copyright 2016-2017, Nokia Corporation
 *
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

package org.onap.vfc.nfvo.driver.vnfm.svnfm.nokia.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;

public class TestStoreLoader {

    private static final String PASSWORD = "password";
    private static final String SUN = "SUN";
    private static final String JKS = "JKS";
    private static final String TEST_JKS = "test.jks";
    private static final String PEM_WITHOUT_BEGIN = "" +
            "INVALID\n" +
            "-----END RSA PRIVATE KEY-----";
    private static final String PEM_WITHOUT_END = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "INVALID\n" +
            "";
    private static final String PEMS_WITHOUT_PRIVATE_KEY = "-----BEGIN CERTIFICATE-----\n" +
            "MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL\n" +
            "MAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC\n" +
            "VU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx\n" +
            "NDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD\n" +
            "TjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu\n" +
            "ZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j\n" +
            "V14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj\n" +
            "gbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA\n" +
            "FFXI70krXeQDxZgbaCQoR4jUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE\n" +
            "CBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS\n" +
            "BgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE\n" +
            "BQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju\n" +
            "Wm7DCfrPNGVwFWUQOmsPue9rZBgO\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL\n" +
            "MAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC\n" +
            "VU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx\n" +
            "NDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD\n" +
            "TjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu\n" +
            "ZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j\n" +
            "V14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj\n" +
            "gbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA\n" +
            "FFXI70krXeQDxZgbaCQoR4tUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE\n" +
            "CBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS\n" +
            "BgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE\n" +
            "BQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju\n" +
            "Wm7DCfrPNGVwFWUQOmsPue9rZBgO\n" +
            "-----END CERTIFICATE-----";
    private static final String PEMS = "-----BEGIN CERTIFICATE-----\n" +
            "MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL\n" +
            "MAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC\n" +
            "VU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx\n" +
            "NDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD\n" +
            "TjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu\n" +
            "ZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j\n" +
            "V14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj\n" +
            "gbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA\n" +
            "FFXI70krXeQDxZgbaCQoR4jUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE\n" +
            "CBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS\n" +
            "BgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE\n" +
            "BQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju\n" +
            "Wm7DCfrPNGVwFWUQOmsPue9rZBgO\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN CERTIFICATE-----\n" +
            "MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL\n" +
            "MAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC\n" +
            "VU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx\n" +
            "NDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD\n" +
            "TjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu\n" +
            "ZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j\n" +
            "V14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj\n" +
            "gbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA\n" +
            "FFXI70krXeQDxZgbaCQoR4tUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE\n" +
            "CBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS\n" +
            "BgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE\n" +
            "BQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju\n" +
            "Wm7DCfrPNGVwFWUQOmsPue9rZBgO\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAF53wUbKmDHtvfOb8u1HPqEBFNNF\n" +
            "csnOMjIcSEhAwIQMbgrOuQ+vH/YgXuuDJaURS85H8P4UTt6lYOJn+SFnXvS82E7LHJpVrWwQzbh2\n" +
            "QKh13/akPe90DlNTUGEYO7rHaPLqTlld0jkLFSytwqfwqn9yrYpM1ncUOpCciK5j8t8MzO71LJoJ\n" +
            "g24CFxpjIS0tBrJvKzrRNcxWSRDLmu2kNmtsh7yyJouE6XoizVmBmNVltHhFaDMmqjugMQA2CZfL\n" +
            "rxiR1ep8TH8IBvPqysqZI1RIpB/e0engP4/1KLrOt+6gGS0JEDh1kG2fJObl+N4n3sCOtgaz5Uz8\n" +
            "8jpwbmZ3Se8CAwEAAQKCAQAdOsSs2MbavAsIM3qo/GBehO0iqdxooMpbQvECmjZ3JTlvUqNkPPWQ\n" +
            "vFdiW8PsHTvtackhdLsqnNUreKxXL5rr8vqi9qm0/0mXpGNi7gP3m/FeaVdYnfpIwgCe6lag5k6M\n" +
            "yv7PG/6N8+XrWyBdwlOe96bGohvB4Jp2YFjSTM67QONQ8CdmfqokqJ8/3RyrpDvGN3iX3yzBqXGO\n" +
            "jPkoJQv3I4lsYdR0nl4obHHnMSeWCQCYvJoZ7ZOliu/Dd0ksItlodG6s8r/ujkSa8VIhe0fnXTf0\n" +
            "i7lqa55CAByGN4MOR0bAkJwIB7nZzQKurBPcTAYJFFvAc5hgMnWT0XW83TehAoGBALVPGnznScUw\n" +
            "O50OXKI5yhxGf/XDT8g28L8Oc4bctRzI+8YfIFfLJ57uDGuojO/BpqtYmXmgORru0jYR8idEkZrx\n" +
            "gf62czOiJrCWTkBCEMtrNfFHQJQCQrjfbHofp7ODnEHbHFm7zdlbfNnEBBaKXxd2rVv4UTEhgftv\n" +
            "wsHcimbXAoGBAIViWrHWElMeQT0datqlThE/u51mcK4VlV7iRWXVa1/gAP85ZAu44VvvDlkpYVkF\n" +
            "zSRR+lHSOzsubDMN45OBQW6UA3RPg4TCvrTOmhQUeF5XPuSdcD0R2At6pdaLwAKnOtILg13Ha6ym\n" +
            "Igjv8glodvem3hWLmpHIhNBiaXtf8wqpAoGADH5a8OhvKOtd8EChGXyp9LDW+HRw9vbyN/gi9dQX\n" +
            "ltgyoUBb1jDllgoJSRHgRFUvyvbb/ImR5c03JwqtiQ8siWTC9G5WGeS+jcSNt9fVmG7W1L14MbrG\n" +
            "Jj8fFns/7xrOlasnlPdgA+5N+CONtI/sZY2D/KZr0drhPhZBcWJlFxkCgYAn+4SOPEo/6hjKNhA6\n" +
            "vER7fSxDEVsDg+rDh3YgAWpvUdlaqBxqOyAqi600YugQZGHK2lv7vNYOdmrunuIx7BPuDqY+bjtR\n" +
            "R4Mc9bVQAZbXSLXMl7j2RWwKfNhLSJbk9LX4EoVtTgLjvOUE4tAdq9fFgpqdwLwzqPTO9kECP4++\n" +
            "CQKBgH6tO/xcNxG/uXUideluAn3H2KeyyznZMJ7oCvzf26/XpTAMI243OoeftiKVMgxuZ7hjwqfn\n" +
            "/VHXABc4i5gchr9RzSb1hZ/IqFzq2YGmbppg5Ok2cgwalDoDBi21bRf8aDRweL62mO+7aPnCQZ58\n" +
            "j5W72PB8BAr6xg0Oro25O4os\n" +
            "-----END RSA PRIVATE KEY-----";
    private static final String PEM = "-----BEGIN CERTIFICATE-----\n" +
            "MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL\n" +
            "MAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC\n" +
            "VU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx\n" +
            "NDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD\n" +
            "TjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu\n" +
            "ZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j\n" +
            "V14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj\n" +
            "gbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA\n" +
            "FFXI70krXeQDxZgbaCQoR4jUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE\n" +
            "CBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS\n" +
            "BgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE\n" +
            "BQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju\n" +
            "Wm7DCfrPNGVwFWUQOmsPue9rZBgO\n" +
            "-----END CERTIFICATE-----\n" +
            "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEuwIBADANBgkqhkiG9w0BAQEFAASCBKUwggShAgEAAoIBAF53wUbKmDHtvfOb8u1HPqEBFNNF\n" +
            "csnOMjIcSEhAwIQMbgrOuQ+vH/YgXuuDJaURS85H8P4UTt6lYOJn+SFnXvS82E7LHJpVrWwQzbh2\n" +
            "QKh13/akPe90DlNTUGEYO7rHaPLqTlld0jkLFSytwqfwqn9yrYpM1ncUOpCciK5j8t8MzO71LJoJ\n" +
            "g24CFxpjIS0tBrJvKzrRNcxWSRDLmu2kNmtsh7yyJouE6XoizVmBmNVltHhFaDMmqjugMQA2CZfL\n" +
            "rxiR1ep8TH8IBvPqysqZI1RIpB/e0engP4/1KLrOt+6gGS0JEDh1kG2fJObl+N4n3sCOtgaz5Uz8\n" +
            "8jpwbmZ3Se8CAwEAAQKCAQAdOsSs2MbavAsIM3qo/GBehO0iqdxooMpbQvECmjZ3JTlvUqNkPPWQ\n" +
            "vFdiW8PsHTvtackhdLsqnNUreKxXL5rr8vqi9qm0/0mXpGNi7gP3m/FeaVdYnfpIwgCe6lag5k6M\n" +
            "yv7PG/6N8+XrWyBdwlOe96bGohvB4Jp2YFjSTM67QONQ8CdmfqokqJ8/3RyrpDvGN3iX3yzBqXGO\n" +
            "jPkoJQv3I4lsYdR0nl4obHHnMSeWCQCYvJoZ7ZOliu/Dd0ksItlodG6s8r/ujkSa8VIhe0fnXTf0\n" +
            "i7lqa55CAByGN4MOR0bAkJwIB7nZzQKurBPcTAYJFFvAc5hgMnWT0XW83TehAoGBALVPGnznScUw\n" +
            "O50OXKI5yhxGf/XDT8g28L8Oc4bctRzI+8YfIFfLJ57uDGuojO/BpqtYmXmgORru0jYR8idEkZrx\n" +
            "gf62czOiJrCWTkBCEMtrNfFHQJQCQrjfbHofp7ODnEHbHFm7zdlbfNnEBBaKXxd2rVv4UTEhgftv\n" +
            "wsHcimbXAoGBAIViWrHWElMeQT0datqlThE/u51mcK4VlV7iRWXVa1/gAP85ZAu44VvvDlkpYVkF\n" +
            "zSRR+lHSOzsubDMN45OBQW6UA3RPg4TCvrTOmhQUeF5XPuSdcD0R2At6pdaLwAKnOtILg13Ha6ym\n" +
            "Igjv8glodvem3hWLmpHIhNBiaXtf8wqpAoGADH5a8OhvKOtd8EChGXyp9LDW+HRw9vbyN/gi9dQX\n" +
            "ltgyoUBb1jDllgoJSRHgRFUvyvbb/ImR5c03JwqtiQ8siWTC9G5WGeS+jcSNt9fVmG7W1L14MbrG\n" +
            "Jj8fFns/7xrOlasnlPdgA+5N+CONtI/sZY2D/KZr0drhPhZBcWJlFxkCgYAn+4SOPEo/6hjKNhA6\n" +
            "vER7fSxDEVsDg+rDh3YgAWpvUdlaqBxqOyAqi600YugQZGHK2lv7vNYOdmrunuIx7BPuDqY+bjtR\n" +
            "R4Mc9bVQAZbXSLXMl7j2RWwKfNhLSJbk9LX4EoVtTgLjvOUE4tAdq9fFgpqdwLwzqPTO9kECP4++\n" +
            "CQKBgH6tO/xcNxG/uXUideluAn3H2KeyyznZMJ7oCvzf26/XpTAMI243OoeftiKVMgxuZ7hjwqfn\n" +
            "/VHXABc4i5gchr9RzSb1hZ/IqFzq2YGmbppg5Ok2cgwalDoDBi21bRf8aDRweL62mO+7aPnCQZ58\n" +
            "j5W72PB8BAr6xg0Oro25O4os\n" +
            "-----END RSA PRIVATE KEY-----";
    private static final String PEM_WITHOUT_PRIVATE_KEY =
            "-----BEGIN CERTIFICATE-----\n" +
                    "MIICUTCCAfugAwIBAgIBADANBgkqhkiG9w0BAQQFADBXMQswCQYDVQQGEwJDTjEL\n" +
                    "MAkGA1UECBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMC\n" +
                    "VU4xFDASBgNVBAMTC0hlcm9uZyBZYW5nMB4XDTA1MDcxNTIxMTk0N1oXDTA1MDgx\n" +
                    "NDIxMTk0N1owVzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAlBOMQswCQYDVQQHEwJD\n" +
                    "TjELMAkGA1UEChMCT04xCzAJBgNVBAsTAlVOMRQwEgYDVQQDEwtIZXJvbmcgWWFu\n" +
                    "ZzBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCp5hnG7ogBhtlynpOS21cBewKE/B7j\n" +
                    "V14qeyslnr26xZUsSVko36ZnhiaO/zbMOoRcKK9vEcgMtcLFuQTWDl3RAgMBAAGj\n" +
                    "gbEwga4wHQYDVR0OBBYEFFXI70krXeQDxZgbaCQoR4jUDncEMH8GA1UdIwR4MHaA\n" +
                    "FFXI70krXeQDxZgbaCQoR4jUDncEoVukWTBXMQswCQYDVQQGEwJDTjELMAkGA1UE\n" +
                    "CBMCUE4xCzAJBgNVBAcTAkNOMQswCQYDVQQKEwJPTjELMAkGA1UECxMCVU4xFDAS\n" +
                    "BgNVBAMTC0hlcm9uZyBZYW5nggEAMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEE\n" +
                    "BQADQQA/ugzBrjjK9jcWnDVfGHlk3icNRq0oV7Ri32z/+HQX67aRfgZu7KWdI+Ju\n" +
                    "Wm7DCfrPNGVwFWUQOmsPue9rZBgO\n" +
                    "-----END CERTIFICATE-----";
    private static final String INVALID_CERT =
            "-----BEGIN CERTIFICATE-----\n" +
                    "MIIC" +
                    "-----END CERTIFICATE-----";
    private static final String PEM_WITH_INVALID_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "INVALID\n" +
            "-----END RSA PRIVATE KEY-----";
    private static final String EMPTY_PEM = "";
    private static KeyStore testStore;

    @BeforeClass
    public static void setUpBefore() throws NoSuchProviderException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        testStore = KeyStore.getInstance(JKS, SUN);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(TEST_JKS);
        String jksFileName = url.getFile();
        File jks = new File(jksFileName);
        FileInputStream fos = new FileInputStream(jks);
        testStore.load(fos, PASSWORD.toCharArray());
    }

    @Test
    public void useInStaticWay() {
        TestUtil.coveragePrivateConstructorForClassesWithStaticMethodsOnly(StoreLoader.class);
    }

    /**
     * test certificate extraction from PEM
     */
    @Test
    public void testCertifate() {
        assertEquals(new HashSet<>(), StoreLoader.getCertifacates(""));
        String pem = "-----BEGIN CERTIFICATE-----\ncontent1\n-----END CERTIFICATE-----\n-----BEGIN CERTIFICATE-----\ncontent2\n-----END CERTIFICATE-----";
        Set<String> certifacates = StoreLoader.getCertifacates(pem);
        assertEquals(newHashSet("-----BEGIN CERTIFICATE-----\ncontent1\n-----END CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\ncontent2\n-----END CERTIFICATE-----"), certifacates);
    }

    /**
     * test keystore loading from PEM with single certificate & key
     */
    @Test
    public void loadStrore() throws Exception {
        //when
        KeyStore ks = StoreLoader.loadStore(PEM, PASSWORD, PASSWORD);

        //then
        assertNotNull(ks.getCertificate(PASSWORD));
        assertTrue(Arrays.equals(testStore.getCertificate(PASSWORD).getPublicKey().getEncoded(), ks.getCertificate(PASSWORD).getPublicKey().getEncoded()));
    }

    /**
     * test keystore loading from PEM with multiple certifcates
     */
    @Test
    public void loadStroreWithCerts() throws Exception {
        //when
        KeyStore ks = StoreLoader.loadStore(PEMS, PASSWORD, PASSWORD);

        //then
        assertNotNull(ks.getCertificate(PASSWORD));
        assertTrue(Arrays.equals(testStore.getCertificate(PASSWORD).getPublicKey().getEncoded(), ks.getCertificate(PASSWORD).getPublicKey().getEncoded()));
    }

    /**
     * test keystore loading from PEM without a private key
     */
    @Test
    public void loadStroreWithCertsWithoutPrivateKey() throws Exception {
        //when
        KeyStore ks = StoreLoader.loadStore(PEMS_WITHOUT_PRIVATE_KEY, PASSWORD, PASSWORD);

        //then
        assertNotNull(ks);
        String alias = PASSWORD + "0";
        assertNotNull(ks.getCertificate(alias));
        assertTrue(Arrays.equals(testStore.getCertificate(PASSWORD).getPublicKey().getEncoded(), ks.getCertificate(alias).getPublicKey().getEncoded()));
    }

    /**
     * non closed BEGIN CERTIFICATE pattern results in empty keystore
     */
    @Test
    public void loadStroreWithPemWithoutBegin() throws Exception {
        //when
        KeyStore ks = StoreLoader.loadStore(PEM_WITHOUT_BEGIN, PASSWORD, PASSWORD);

        //then
        assertNull(ks.getCertificate(PASSWORD));
    }

    /**
     * non closed END CERTIFICATE pattern results in empty keystore
     */
    @Test
    public void loadStroreWithPemWithoutEnd() throws Exception {
        //when
        KeyStore ks = StoreLoader.loadStore(PEM_WITHOUT_END, PASSWORD, PASSWORD);

        //then
        assertNull(ks.getCertificate(PASSWORD));
    }

    /**
     * empty PEM results in empty keystore
     */
    @Test
    public void loadStroreWithPemWithoutBeginAndEnd() throws Exception {
        //when
        KeyStore ks = StoreLoader.loadStore(EMPTY_PEM, PASSWORD, PASSWORD);

        //then
        assertNull(ks.getCertificate(PASSWORD));
    }

    /**
     * invalid private key results in an error
     */
    @Test
    public void loadStroreWithPemWithInvalidKey() throws Exception {
        //when
        try {
            StoreLoader.loadStore(PEM_WITH_INVALID_PRIVATE_KEY, PASSWORD, PASSWORD);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to load key", e.getMessage());

        }
    }

    /**
     * invalid password results in error
     */
    @Test
    public void testInvlaidPassword() throws Exception {
        //when
        try {
            StoreLoader.loadStore(PEM, null, null);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to create keystore", e.getMessage());
        }
    }

    /**
     * invalid certificate results in error
     */
    @Test
    public void testInvlaidCertificate() throws Exception {
        //when
        try {
            StoreLoader.loadStore(INVALID_CERT, null, null);
            fail();
        } catch (Exception e) {
            assertEquals("Unable to load certificates", e.getMessage());

        }
    }
}
