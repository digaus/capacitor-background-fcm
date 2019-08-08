
  Pod::Spec.new do |s|
    s.name = 'CapacitorBackgroundFcm'
    s.version = '0.0.2'
    s.summary = 'Background FCM handling'
    s.license = 'MIT'
    s.homepage = 'https://github.com/digaus/capacitor-background-fcm'
    s.author = 'Dirk Gausmann'
    s.source = { :git => 'https://github.com/digaus/capacitor-background-fcm', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end