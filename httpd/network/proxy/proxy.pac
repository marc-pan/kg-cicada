function FindProxyForURL(url, host) {
  /* Normalize the URL for pattern matching */
  url = url.toLowerCase();
  host = host.toLowerCase();

  /* Don't proxy local hostnames */
  if (isPlainHostName(host)) {
    return 'DIRECT';
  }

  // Declare a variable to store the result of DNS resolution
  // Avoids multiple lookups (even from cache) and DNS A records with multiple mappings
  var host_ip = dnsResolve(host)

  // Declare a variable to store the protocol of the request
  // Extract the protocol from the URL using the substring and indexOf methods
  var protocol = url.substring(0, url.indexOf(":") - 1);

  // Access the internet directly for one site
  if (dnsDomainIs(host, ".ibm.com") ||
    dnsDomainIs(host, ".baidu.com") ||
    dnsDomainIs(host, ".aliyundrive.com") ||
    dnsDomainIs(host, ".ibm.biz") ||
    dnsDomainIs(host, ".swg-devops.com")) {
    return "DIRECT";
  }

  // No proxy for private (RFC 1918) IP addresses (intranet sites)
  // Using host_ip variable simplifies code for easier reading
  if (isInNet(host_ip, "10.0.0.0", "255.0.0.0") ||
    isInNet(host_ip, "9.0.0.0", "255.0.0.0") ||
    isInNet(host_ip, "172.16.0.0", "255.240.0.0") ||
    isInNet(host_ip, "192.168.0.0", "255.255.0.0")) {
    return "DIRECT";
  }

  // No proxy for localhost
  if (isInNet(host, "127.0.0.0", "255.0.0.0")) {
    return "DIRECT";
  }

  // Clean-up rule. Everything else uses a proxy. Note semi-colon delimiter between strings.
  return "PROXY turquoise-vm.fyre.ibm.com:8080; PROXY teal-vm.fyre.ibm.com:8081; DIRECT";
}
