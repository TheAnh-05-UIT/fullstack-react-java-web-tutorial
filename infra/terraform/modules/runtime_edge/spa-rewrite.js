function handler(event) {
  var request = event.request;
  var uri = request.uri;

  if (uri.startsWith('/api/') || uri.startsWith('/uploads/')) {
    return request;
  }

  var lastSegment = uri.substring(uri.lastIndexOf('/') + 1);
  if (uri.endsWith('/') || !lastSegment.includes('.')) {
    request.uri = '/index.html';
  }

  return request;
}
