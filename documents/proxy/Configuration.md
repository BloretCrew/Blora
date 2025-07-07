# 配置文件

---

## administration

服务器管理相关配置。

### debug

`布尔值`  
调试模式。开启后登录、注册的 Dialog 可以按 ESC 直接关闭。

### logging（WIP）

`布尔值`  
日志输出。开启后会输出插件的所有日志。

---

## mail

邮件系统相关的设置。

### senderNameFormat

`字符串`  
发件人的名字的显示格式。  
支持的替换符：

- \<username>：玩家的名字
- 当玩家在线且与收件人所处同一服务器时支持 PAPI

---

## server

登录系统的服务器相关配置。

### limbo

`字符串`  
登录使用的临时服务器，填写的值应为一个在 Velocity 的配置文件中注册的服务器名称。

### lobby

`字符串`  
主大厅，玩家登录后将会被传送到该服务器（仅在未开启“始终传送至大厅”的情况下）。

---

## messaging

服务器间通信系统的设置，插件部分重要功能都依赖此功能。

### port

`32 位整型值`  
通信服务器开设的端口。

### password

`字符串`  
服务器通信密码，客户服务器连接时发送的验证包需要包含正确的密码才能建立连接。

---

## authorization

登录验证系统的设置。

### onlineFeatures

`布尔值`  
是否开启正版验证相关功能，只有这个选项开启其他正版选项才会有效。

### alwaysLobby

`布尔值`  
玩家登录后是否强制传送至大厅服务器，设置为 false 则会将玩家传送回之前所在的服务器。

### checkUsername

`布尔值`  
启用用户名安全检查。

### usernameRegex

`正则表达式`  
用户名字符正则检测，请勿修改长度检测，保持为 *（无限长度）。

### minUsernameLength

`32 位整型值`  
用户名的最小长度。

### maxUsernameLength

`32 位整型值`  
用户名的最大长度。

### uuidGenerator

`枚举：MOJANG, CHECKED`

玩家 UUID 生成器。

- MOJANG：如果这个玩家名存在正版用户，则优先使用正版 UUID，如果原本是离线玩家但后期购买了正版，UUID 会保持为离线的
  UUID（推荐使用）
- CRACKED：所有玩家都按离线玩家计算，缺点是正版用户改名后会因为 UUID 的改变而导致数据丢失

---

## security

安全相关设置。

### allowOnlinePlayerAutoLogin

`布尔值`  
是否允许正版玩家自动登录，关闭后正版玩家也需要像盗版玩家一样输入密码进行注册和登录。

### sameIpAutoLogin

`布尔值`  
允许玩家退出游戏后，在同一 IP 下再次登录时允许自动登录。

### autoLoginExpireTime

`64 位整型值`  
自动登录的过期时间，单位为秒。

### minPasswordLength

`32 位整型值`  
密码的最小长度。

### maxPasswordLength

`32 位整型值`
密码的最大长度。

### weakPasswords

`字符串数组`  
定义一系列弱密码，用户无法使用这些密码，不区分大小写。

### passwordStrategy

`字符串数组`
密码策略，当前支持的策略如下：

- noUsername：禁止密码中出现玩家的名称，无论如何大小写改变
- noDuplicated:{数字}：禁止同一个字符连续 {数字} 次及以上次数出现，无论大小写
- noConsecutive:{数字}：禁止出现类似 abc, zyx, hij, 123, 567 这样无论正序、倒序连续的字母超过 {数字} 个排列
- uppercaseIncluded：必须出现大写字母
- numberIncluded：必须出现数字
- symbolIncluded：必须出现符号

### ipLimit

`32 位整型值`  
IP 限制，小于等于 0 不生效。

### ipLimitDisableRegister

`布尔值`  
关闭同一 IP 注册的限制

### ipLimitDisableLogin

`布尔值`  
关闭同一 IP 登录的限制

### ipLimitStrategyForRegister

`枚举：FIRST, LAST`  
对于注册的 IP 限制策略。

- FIRST：保存所有玩家首次进入服务器的 IP，然后进行注册限制
- LAST：保存所有玩家最后一次进入服务器的 IP，然后进行注册限制

### maxRetries

`32 位整型值`
最大的重试次数，超出将踢出，小于等于 0 的数字则为不限制。

### maxNotLogin

`32 位整型值`
未登录时最大在线时间（秒），超时将自动踢出，小于等于 0 的数字则为不限制。

---

## database

服务器数据库连接设置。

### host

`字符串`

### port

`32 位整型值`

### username

`字符串`

### password

`字符串`

### database

`字符串`

### maxLifeTime

`64 位整型值`    
数据库连接保活时间（毫秒），超过时间后使用数据库需重连。

### jdbcUrl

`字符串`  
连接数据库时使用的 jdbc url，不了解的情况下不建议修改。

## ~~messages~~

文本信息，未来将会有专门的本地化文件