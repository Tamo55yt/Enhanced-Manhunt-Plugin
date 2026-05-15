Write-Host "Manhunt Multi-Platform & Multi-Version Build Scripti basliyor..." -ForegroundColor Cyan

# Eski JAR'ları temizle
Write-Host "Eski JAR dosyalari temizleniyor..." -ForegroundColor Gray
Remove-Item *.jar -ErrorAction SilentlyContinue

$profiles = @(
    @{ id = "v26_1"; tag = "26.x" },
    @{ id = "v1_21"; tag = "1.21.x" },
    @{ id = "v1_20"; tag = "1.20.x" },
    @{ id = "v1_19"; tag = "1.19.x" },
    @{ id = "v1_18"; tag = "1.18.x" },
    @{ id = "v1_17"; tag = "1.17.x" },
    @{ id = "v1_16"; tag = "1.16.x" },
    @{ id = "v1_15"; tag = "1.15.x" },
    @{ id = "v1_14"; tag = "1.14.x" },
    @{ id = "v1_13"; tag = "1.13.x" },
    @{ id = "v1_12"; tag = "1.12.x" },
    @{ id = "v1_11"; tag = "1.11.x" },
    @{ id = "v1_10"; tag = "1.10.x" },
    @{ id = "v1_9";  tag = "1.9.x"  },
    @{ id = "v1_8";  tag = "1.8.x"  }
)

foreach ($p in $profiles) {
    $id = $p.id
    $tag = $p.tag
    
    Write-Host "------------------------------------------" -ForegroundColor White
    Write-Host "$tag sürümü için derleme basliyor..." -ForegroundColor Yellow
    
    # Bukkit Build
    Write-Host "[$tag] Bukkit derleniyor..." -ForegroundColor Gray
    mvn clean package -P $id -pl bukkit -am -DskipTests -q
    
    if ($LASTEXITCODE -eq 0) {
        if (Test-Path "bukkit\target\manhunt-bukkit-1.1.jar") {
            Copy-Item "bukkit\target\manhunt-bukkit-1.1.jar" -Destination ".\Manhunt-Bukkit_$tag.jar" -Force
            Write-Host "[$tag] Bukkit hazir: Manhunt-Bukkit_$tag.jar" -ForegroundColor Green
        }
    } else {
        Write-Host "[$tag] Bukkit derlenirken HATA olustu!" -ForegroundColor Red
    }

    # Sponge Build
    Write-Host "[$tag] Sponge derleniyor..." -ForegroundColor Gray
    mvn clean package -P $id -pl sponge -am -DskipTests -q
    
    if ($LASTEXITCODE -eq 0) {
        if (Test-Path "sponge\target\manhunt-sponge-1.1.jar") {
            Copy-Item "sponge\target\manhunt-sponge-1.1.jar" -Destination ".\Manhunt-Sponge_$tag.jar" -Force
            Write-Host "[$tag] Sponge hazir: Manhunt-Sponge_$tag.jar" -ForegroundColor Green
        }
    } else {
        Write-Host "[$tag] Sponge derlenirken HATA olustu! (API farkliliklari olabilir)" -ForegroundColor Red
    }
}

Write-Host "------------------------------------------" -ForegroundColor White
Write-Host "Tum islemler tamamlandi. Basarili JAR dosyalari ana dizinde hazır." -ForegroundColor Cyan
