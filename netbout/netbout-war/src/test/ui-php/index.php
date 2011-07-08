<?php
/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email: privacy@netbout.com.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

$home = dirname(__FILE__);
$src = realpath($home . '/../../main/webapp');
$dest = '/tmp/netbout-ui';
if (!file_exists($dest)) {
    mkdir($dest);
}
shell_exec('rm -rf ' . $dest . '/*');
shell_exec('cp -R ' . $src . '/* ' . $dest);
shell_exec('cp -R ' . $home . '/data/* ' . $dest);

if (isset($_GET['path'])) {
    $path = $_GET['path'];
} else {
    $path = 'PageWithBouts.xml';
}

if (preg_match('/\.xml$/', $path)) {
    $type = 'text/xml';
} else if (preg_match('/\.xsl$/', $path)) {
    $type = 'text/xsl';
} else if (preg_match('/\.css$/', $path)) {
    $type = 'text/css';
} else if (preg_match('/\.png$/', $path)) {
    $type = 'image/png';
}
header('Content-Type:' . $type);
$content = file_get_contents($dest . '/' . $path);
echo preg_replace('/href=(?:"|\')(.*?)(?:"|\')/', 'href=\'?path=${1}\'', $content);

