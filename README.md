# FlightDemo


By downloading, copying, installing or using the software you agree to this license.
If you do not agree to this license, do not download, install,
copy or use the software.


                          License Agreement
               For Open Source Computer Vision Library
                       (3-clause BSD License)

Copyright (C) 2015-2016, saki t_saki@serenegiant.com

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the names of the copyright holders nor the names of the contributors
    may be used to endorse or promote products derived from this software
    without specific prior written permission.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are disclaimed.
In no event shall copyright holders or contributors be liable for any direct,
indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused
and on any theory of liability, whether in contract, strict liability,
or tort (including negligence or otherwise) arising in any way out of
the use of this software, even if advised of the possibility of such damage.

### ビルド方法
プロジェクト直下に`local.properties`というファイルを作成し、`sdk.dir`と`ndk.dir`を正しく設定してください。

```
sdk.dir={Android SDKへのパス}
ndk.dir={Android NDKへのパス}
```

### リリースビルドするには
プロジェクト直下に`local.properties`というファイルにリリース署名用の設定を追加してください。
キーストアの作り方などはWebで^^;

```
KEYSTORE_ACE_PARROT={キーストアパスワード}
STORE_PASSWORD_ACE_PARROT={キーストアファイルのパス}
KEY_PASSWORD_ACEPARROT={エリアスのパスワード}
ALIAS_ACEPARROT={エリアス名}

KEYSTORE_AUTO_PARROT={キーストアパスワード}
STORE_PASSWORD_AUTO_PARROT={キーストアファイルのパス}
KEY_PASSWORD_AUTO_PARROT={エリアスのパスワード}
ALIAS_AUTO_PARROT={エリアス名}
```
